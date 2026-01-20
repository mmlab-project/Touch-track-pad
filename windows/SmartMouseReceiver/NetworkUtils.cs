using System.Net;
using System.Net.NetworkInformation;
using System.Net.Sockets;

namespace SmartMouseReceiver;

/// <summary>
/// ネットワークアダプタ情報とIPアドレス取得ユーティリティ
/// </summary>
public static class NetworkUtils
{
    /// <summary>
    /// 除外するアダプタ名のキーワード（仮想アダプタ等）
    /// </summary>
    private static readonly string[] ExcludedAdapterKeywords =
    [
        "Virtual", "VMware", "VirtualBox", "Hyper-V", "WSL",
        "Docker", "vEthernet", "Loopback", "Bluetooth", "Tunnel"
    ];

    /// <summary>
    /// 利用可能なIPv4アドレス一覧を取得（仮想アダプタ除外）
    /// </summary>
    public static List<NetworkAdapterInfo> GetAvailableNetworkAdapters()
    {
        var adapters = new List<NetworkAdapterInfo>();

        foreach (var nic in NetworkInterface.GetAllNetworkInterfaces())
        {
            // 無効なアダプタはスキップ
            if (nic.OperationalStatus != OperationalStatus.Up)
                continue;

            // 除外キーワードに一致するアダプタはスキップ
            if (ExcludedAdapterKeywords.Any(keyword =>
                nic.Name.Contains(keyword, StringComparison.OrdinalIgnoreCase) ||
                nic.Description.Contains(keyword, StringComparison.OrdinalIgnoreCase)))
                continue;

            var ipProps = nic.GetIPProperties();
            foreach (var addr in ipProps.UnicastAddresses)
            {
                // IPv4のみ、ループバック除外
                if (addr.Address.AddressFamily == AddressFamily.InterNetwork &&
                    !IPAddress.IsLoopback(addr.Address))
                {
                    adapters.Add(new NetworkAdapterInfo
                    {
                        Name = nic.Name,
                        Description = nic.Description,
                        IpAddress = addr.Address.ToString(),
                        InterfaceType = nic.NetworkInterfaceType,
                        Priority = GetAdapterPriority(nic)
                    });
                }
            }
        }

        // 優先度順にソート
        return adapters.OrderByDescending(a => a.Priority).ToList();
    }

    /// <summary>
    /// アダプタの優先度を計算
    /// </summary>
    private static int GetAdapterPriority(NetworkInterface nic)
    {
        return nic.NetworkInterfaceType switch
        {
            NetworkInterfaceType.Ethernet => 100,        // 有線が最優先
            NetworkInterfaceType.Wireless80211 => 80,    // Wi-Fi次点
            NetworkInterfaceType.GigabitEthernet => 90,
            _ => 50
        };
    }

    /// <summary>
    /// 最適なIPアドレスを自動選択
    /// </summary>
    public static string? GetBestIpAddress()
    {
        var adapters = GetAvailableNetworkAdapters();
        return adapters.FirstOrDefault()?.IpAddress;
    }

    /// <summary>
    /// 認証トークンを生成
    /// </summary>
    public static string GenerateToken(int length = 32)
    {
        const string chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        var random = new Random();
        return new string(Enumerable.Repeat(chars, length)
            .Select(s => s[random.Next(s.Length)]).ToArray());
    }
}

/// <summary>
/// ネットワークアダプタ情報
/// </summary>
public class NetworkAdapterInfo
{
    public required string Name { get; init; }
    public required string Description { get; init; }
    public required string IpAddress { get; init; }
    public NetworkInterfaceType InterfaceType { get; init; }
    public int Priority { get; init; }

    public override string ToString() => $"{Name} ({IpAddress})";
}
