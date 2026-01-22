using System.Net;
using System.Net.Sockets;

namespace GlideDeckReceiver;

/// <summary>
/// 接続クライアントのセッション管理
/// </summary>
public class ClientSession
{
    public required string Token { get; init; }
    public required IPEndPoint TcpEndPoint { get; set; }
    public IPEndPoint? UdpEndPoint { get; set; }
    public TcpClient? TcpClient { get; set; }
    public NetworkStream? Stream { get; set; }
    public bool IsAuthenticated { get; set; }
    public DateTime LastActivity { get; set; } = DateTime.UtcNow;
    public int ProtocolVersion { get; set; }
    public string? DeviceName { get; set; }

    /// <summary>
    /// セッションタイムアウト判定（5分）
    /// </summary>
    public bool IsTimedOut => (DateTime.UtcNow - LastActivity).TotalMinutes > 5;

    /// <summary>
    /// アクティビティ更新
    /// </summary>
    public void UpdateActivity()
    {
        LastActivity = DateTime.UtcNow;
    }

    /// <summary>
    /// リソース解放
    /// </summary>
    public void Dispose()
    {
        try
        {
            Stream?.Close();
            TcpClient?.Close();
        }
        catch { /* ignore */ }
    }
}

/// <summary>
/// クライアントセッション管理マネージャー
/// </summary>
public class SessionManager
{
    private readonly Dictionary<string, ClientSession> _sessions = new();
    private readonly Dictionary<string, ClientSession> _udpEndpointMap = new();
    private readonly object _lock = new();

    public event Action<ClientSession>? OnClientConnected;
    public event Action<ClientSession>? OnClientDisconnected;

    /// <summary>
    /// 新規セッション作成
    /// </summary>
    public ClientSession CreateSession(string token, IPEndPoint tcpEndPoint)
    {
        lock (_lock)
        {
            var session = new ClientSession
            {
                Token = token,
                TcpEndPoint = tcpEndPoint
            };
            _sessions[token] = session;
            return session;
        }
    }

    /// <summary>
    /// トークンでセッション取得
    /// </summary>
    public ClientSession? GetSession(string token)
    {
        lock (_lock)
        {
            return _sessions.TryGetValue(token, out var session) ? session : null;
        }
    }

    /// <summary>
    /// UDP EndPointでセッション取得
    /// </summary>
    public ClientSession? GetSessionByUdpEndpoint(IPEndPoint endpoint)
    {
        lock (_lock)
        {
            var key = endpoint.ToString();
            return _udpEndpointMap.TryGetValue(key, out var session) ? session : null;
        }
    }

    /// <summary>
    /// UDP EndPoint登録
    /// </summary>
    public void RegisterUdpEndpoint(ClientSession session, IPEndPoint endpoint)
    {
        lock (_lock)
        {
            session.UdpEndPoint = endpoint;
            _udpEndpointMap[endpoint.ToString()] = session;
        }
    }

    /// <summary>
    /// セッション認証完了
    /// </summary>
    public void AuthenticateSession(string token, int version, string? deviceName = null)
    {
        lock (_lock)
        {
            if (_sessions.TryGetValue(token, out var session))
            {
                session.IsAuthenticated = true;
                session.ProtocolVersion = version;
                session.DeviceName = deviceName;
                session.UpdateActivity();
                OnClientConnected?.Invoke(session);
            }
        }
    }

    /// <summary>
    /// セッション削除
    /// </summary>
    public void RemoveSession(string token)
    {
        lock (_lock)
        {
            if (_sessions.TryGetValue(token, out var session))
            {
                if (session.UdpEndPoint != null)
                {
                    _udpEndpointMap.Remove(session.UdpEndPoint.ToString());
                }
                session.Dispose();
                _sessions.Remove(token);
                OnClientDisconnected?.Invoke(session);
            }
        }
    }

    /// <summary>
    /// タイムアウトセッションをクリーンアップ
    /// </summary>
    public void CleanupTimedOutSessions()
    {
        lock (_lock)
        {
            var timedOut = _sessions.Values.Where(s => s.IsTimedOut).ToList();
            foreach (var session in timedOut)
            {
                RemoveSession(session.Token);
            }
        }
    }

    /// <summary>
    /// 接続中クライアント数
    /// </summary>
    public int ActiveClientCount
    {
        get
        {
            lock (_lock)
            {
                return _sessions.Values.Count(s => s.IsAuthenticated);
            }
        }
    }

    /// <summary>
    /// 全セッション取得
    /// </summary>
    public IReadOnlyList<ClientSession> GetAllSessions()
    {
        lock (_lock)
        {
            return _sessions.Values.ToList();
        }
    }
}

