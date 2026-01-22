using System.Net;
using System.Net.Sockets;
using System.Text;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace GlideDeckReceiver;

/// <summary>
/// TCP/UDPサーバーホスト
/// </summary>
public class ServerHost : IDisposable
{
    private const int CurrentProtocolVersion = 1;
    private const int DefaultPort = 50000;

    private TcpListener? _tcpListener;
    private UdpClient? _udpClient;
    private CancellationTokenSource? _cts;
    private readonly SessionManager _sessionManager;
    private string _currentToken = "";

    public event Action<string>? OnLog;
    public event Action<int>? OnClientCountChanged;

    public string CurrentIp { get; private set; } = "";
    public int Port { get; private set; } = DefaultPort;
    public string Token => _currentToken;
    public bool IsRunning => _tcpListener != null;

    public MacroManager MacroManager { get; } = new MacroManager();

    public ServerHost()
    {
        _sessionManager = new SessionManager();
        _sessionManager.OnClientConnected += _ => OnClientCountChanged?.Invoke(_sessionManager.ActiveClientCount);
        _sessionManager.OnClientDisconnected += _ => OnClientCountChanged?.Invoke(_sessionManager.ActiveClientCount);
    }

    /// <summary>
    /// サーバー開始
    /// </summary>
    public async Task StartAsync(string ipAddress, int port = DefaultPort)
    {
        if (IsRunning) return;

        CurrentIp = ipAddress;
        Port = port;
        _currentToken = NetworkUtils.GenerateToken();
        _cts = new CancellationTokenSource();

        var ip = IPAddress.Parse(ipAddress);

        // TCPサーバー開始
        _tcpListener = new TcpListener(ip, port);
        _tcpListener.Start();
        Log($"TCP Server started on {ipAddress}:{port}");

        // UDPサーバー開始
        _udpClient = new UdpClient(new IPEndPoint(ip, port));
        Log($"UDP Server started on {ipAddress}:{port}");

        // 接続受付開始
        _ = AcceptTcpClientsAsync(_cts.Token);
        _ = ReceiveUdpPacketsAsync(_cts.Token);
        _ = CleanupSessionsAsync(_cts.Token);
    }

    /// <summary>
    /// サーバー停止
    /// </summary>
    public void Stop()
    {
        _cts?.Cancel();
        _tcpListener?.Stop();
        _tcpListener = null;
        _udpClient?.Close();
        _udpClient = null;
        Log("Server stopped");
    }

    /// <summary>
    /// QRコード用JSON生成
    /// </summary>
    public string GetQrCodeJson()
    {
        return JsonConvert.SerializeObject(new
        {
            ip = CurrentIp,
            port = Port,
            token = _currentToken,
            ver = CurrentProtocolVersion
        });
    }

    /// <summary>
    /// TCP接続受付
    /// </summary>
    private async Task AcceptTcpClientsAsync(CancellationToken ct)
    {
        while (!ct.IsCancellationRequested && _tcpListener != null)
        {
            try
            {
                var client = await _tcpListener.AcceptTcpClientAsync(ct);
                _ = HandleTcpClientAsync(client, ct);
            }
            catch (OperationCanceledException) { break; }
            catch (Exception ex)
            {
                Log($"TCP Accept error: {ex.Message}");
            }
        }
    }

    /// <summary>
    /// TCPクライアント処理
    /// </summary>
    private async Task HandleTcpClientAsync(TcpClient client, CancellationToken ct)
    {
        var endpoint = (IPEndPoint)client.Client.RemoteEndPoint!;
        Log($"TCP connection from {endpoint}");

        ClientSession? session = null;
        var stream = client.GetStream();
        var buffer = new byte[4096];

        try
        {
            while (!ct.IsCancellationRequested && client.Connected)
            {
                var bytesRead = await stream.ReadAsync(buffer, ct);
                if (bytesRead == 0) break;

                var message = Encoding.UTF8.GetString(buffer, 0, bytesRead);
                
                // 複数メッセージが連結される可能性があるため分割
                foreach (var line in message.Split('\n', StringSplitOptions.RemoveEmptyEntries))
                {
                    session = await ProcessTcpMessageAsync(line.Trim(), session, client, stream, endpoint);
                }
            }
        }
        catch (Exception ex)
        {
            Log($"TCP Client error: {ex.Message}");
        }
        finally
        {
            if (session != null)
            {
                _sessionManager.RemoveSession(session.Token);
            }
            client.Close();
            Log($"TCP connection closed: {endpoint}");
        }
    }

    /// <summary>
    /// TCPメッセージ処理
    /// </summary>
    private async Task<ClientSession?> ProcessTcpMessageAsync(
        string message, ClientSession? session, TcpClient client, 
        NetworkStream stream, IPEndPoint endpoint)
    {
        try
        {
            var json = JObject.Parse(message);
            var type = json["type"]?.ToString();

            switch (type)
            {
                case "AUTH":
                    return await HandleAuthAsync(json, client, stream, endpoint);

                case "CLICK":
                    if (session?.IsAuthenticated == true)
                    {
                        session.UpdateActivity();
                        var button = ParseMouseButton(json["button"]?.ToString());
                        InputSynthesizer.MouseClick(button);
                    }
                    break;

                case "MOUSE_DOWN":
                    if (session?.IsAuthenticated == true)
                    {
                        session.UpdateActivity();
                        var button = ParseMouseButton(json["button"]?.ToString());
                        InputSynthesizer.MouseDown(button);
                    }
                    break;

                case "MOUSE_UP":
                    if (session?.IsAuthenticated == true)
                    {
                        session.UpdateActivity();
                        var button = ParseMouseButton(json["button"]?.ToString());
                        InputSynthesizer.MouseUp(button);
                    }
                    break;

                case "KEY":
                    if (session?.IsAuthenticated == true)
                    {
                        session.UpdateActivity();
                        HandleKeyInput(json);
                    }
                    break;

                case "TEXT":
                    if (session?.IsAuthenticated == true)
                    {
                        session.UpdateActivity();
                        var text = json["text"]?.ToString();
                        if (!string.IsNullOrEmpty(text))
                        {
                            InputSynthesizer.TypeString(text);
                        }
                    }
                    break;

                case "CLIPBOARD":
                    if (session?.IsAuthenticated == true)
                    {
                        session.UpdateActivity();
                        var clipText = json["text"]?.ToString();
                        var source = json["source"]?.ToString();
                        if (!string.IsNullOrEmpty(clipText) && source == "ANDROID")
                        {
                            ClipboardSync.SetClipboardText(clipText, isFromRemote: true);
                        }
                    }
                    break;

                case "PING":
                    if (session?.IsAuthenticated == true)
                    {
                        session.UpdateActivity();
                        var pong = JsonConvert.SerializeObject(new { type = "PONG", time = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds() });
                        await SendTcpAsync(stream, pong);
                    }
                    break;

                case "GET_MACROS":
                    if (session?.IsAuthenticated == true)
                    {
                        session.UpdateActivity();
                        var macros = MacroManager.Macros.Select(m => new { id = m.Id, name = m.Name }).ToList();
                        var response = JsonConvert.SerializeObject(new { type = "MACROS", macros });
                        await SendTcpAsync(stream, response);
                    }
                    break;

                case "EXEC_MACRO":
                    if (session?.IsAuthenticated == true)
                    {
                        session.UpdateActivity();
                        var id = json["id"]?.ToString();
                        if (!string.IsNullOrEmpty(id))
                        {
                            MacroManager.executeMacro(id);
                        }
                    }
                    break;
            }
        }
        catch (Exception ex)
        {
            Log($"Message parse error: {ex.Message}");
        }

        return session;
    }

    /// <summary>
    /// 認証処理
    /// </summary>
    private async Task<ClientSession?> HandleAuthAsync(JObject json, TcpClient client, NetworkStream stream, IPEndPoint endpoint)
    {
        var token = json["token"]?.ToString();
        var version = json["version"]?.ToObject<int>() ?? 0;
        var deviceName = json["device"]?.ToString();

        if (token != _currentToken)
        {
            Log($"Auth failed: Invalid token from {endpoint}");
            await SendTcpAsync(stream, JsonConvert.SerializeObject(new { type = "AUTH_RESULT", success = false, error = "Invalid token" }));
            return null;
        }

        if (version != CurrentProtocolVersion)
        {
            Log($"Auth failed: Version mismatch (client: {version}, server: {CurrentProtocolVersion})");
            await SendTcpAsync(stream, JsonConvert.SerializeObject(new { type = "AUTH_RESULT", success = false, error = "Version mismatch. Please update the app." }));
            return null;
        }

        // セッション作成
        var session = _sessionManager.CreateSession(token, endpoint);
        session.TcpClient = client;
        session.Stream = stream;
        _sessionManager.AuthenticateSession(token, version, deviceName);

        Log($"Auth success: {deviceName ?? "Unknown"} ({endpoint})");
        await SendTcpAsync(stream, JsonConvert.SerializeObject(new { type = "AUTH_RESULT", success = true }));

        return session;
    }

    /// <summary>
    /// キー入力処理
    /// </summary>
    private void HandleKeyInput(JObject json)
    {
        var code = json["code"]?.ToString();
        var modifiers = json["modifiers"]?.ToObject<List<string>>() ?? [];

        if (string.IsNullOrEmpty(code)) return;

        // 修飾キーを変換
        var mods = new List<VirtualKey>();
        foreach (var mod in modifiers)
        {
            var modKey = mod.ToUpperInvariant() switch
            {
                "CTRL" or "CONTROL" => VirtualKey.LControl,
                "ALT" => VirtualKey.LMenu,
                "SHIFT" => VirtualKey.LShift,
                "WIN" or "META" => VirtualKey.LWin,
                _ => (VirtualKey?)null
            };
            if (modKey.HasValue) mods.Add(modKey.Value);
        }

        // キーコードを変換
        if (Enum.TryParse<VirtualKey>(code, true, out var vk))
        {
            if (mods.Count > 0)
            {
                InputSynthesizer.KeyPressWithModifiers(vk, [.. mods]);
            }
            else
            {
                InputSynthesizer.KeyPress(vk);
            }
        }
    }

    /// <summary>
    /// UDPパケット受信
    /// </summary>
    private async Task ReceiveUdpPacketsAsync(CancellationToken ct)
    {
        while (!ct.IsCancellationRequested && _udpClient != null)
        {
            try
            {
                var result = await _udpClient.ReceiveAsync(ct);
                ProcessUdpPacket(result.Buffer, result.RemoteEndPoint);
            }
            catch (OperationCanceledException) { break; }
            catch (Exception ex)
            {
                Log($"UDP error: {ex.Message}");
            }
        }
    }

    /// <summary>
    /// UDPパケット処理
    /// </summary>
    private void ProcessUdpPacket(byte[] data, IPEndPoint endpoint)
    {
        try
        {
            var message = Encoding.UTF8.GetString(data);
            var parts = message.Split('|');
            if (parts.Length < 3) return;

            var token = parts[0];
            
            // トークン検証 - 未認証パケットは破棄
            if (token != _currentToken)
            {
                return;
            }

            var session = _sessionManager.GetSession(token);
            if (session == null || !session.IsAuthenticated)
            {
                return;
            }

            session.UpdateActivity();

            // UDP EndPoint登録
            if (session.UdpEndPoint == null)
            {
                _sessionManager.RegisterUdpEndpoint(session, endpoint);
            }

            var type = parts[1];

            switch (type)
            {
                case "M" when parts.Length >= 4: // Mouse Move
                    if (int.TryParse(parts[2], out var dx) && int.TryParse(parts[3], out var dy))
                    {
                        InputSynthesizer.MoveMouse(dx, dy);
                    }
                    break;

                case "S" when parts.Length >= 4: // Scroll
                    if (int.TryParse(parts[2], out var sx) && int.TryParse(parts[3], out var sy))
                    {
                        if (sy != 0) InputSynthesizer.MouseWheel(sy);
                        if (sx != 0) InputSynthesizer.MouseHWheel(sx);
                    }
                    break;
            }
        }
        catch (Exception ex)
        {
            Log($"UDP parse error: {ex.Message}");
        }
    }

    /// <summary>
    /// セッションクリーンアップ
    /// </summary>
    private async Task CleanupSessionsAsync(CancellationToken ct)
    {
        while (!ct.IsCancellationRequested)
        {
            await Task.Delay(TimeSpan.FromMinutes(1), ct);
            _sessionManager.CleanupTimedOutSessions();
        }
    }

    /// <summary>
    /// TCPメッセージ送信
    /// </summary>
    private async Task SendTcpAsync(NetworkStream stream, string message)
    {
        var data = Encoding.UTF8.GetBytes(message + "\n");
        await stream.WriteAsync(data);
    }

    /// <summary>
    /// クリップボードテキスト送信（全クライアント）
    /// </summary>
    public async Task BroadcastClipboardAsync(string text)
    {
        var message = JsonConvert.SerializeObject(new
        {
            type = "CLIPBOARD",
            text,
            source = "WINDOWS"
        }) + "\n";

        var data = Encoding.UTF8.GetBytes(message);

        foreach (var session in _sessionManager.GetAllSessions())
        {
            if (session.IsAuthenticated && session.Stream != null)
            {
                try
                {
                    await session.Stream.WriteAsync(data);
                }
                catch { /* ignore */ }
            }
        }
    }

    private static MouseButton ParseMouseButton(string? button)
    {
        return button?.ToUpperInvariant() switch
        {
            "RIGHT" => MouseButton.Right,
            "MIDDLE" => MouseButton.Middle,
            _ => MouseButton.Left
        };
    }

    private void Log(string message)
    {
        OnLog?.Invoke($"[{DateTime.Now:HH:mm:ss}] {message}");
    }

    public void Dispose()
    {
        Stop();
        GC.SuppressFinalize(this);
    }
}

