using System.Runtime.InteropServices;
using System.Windows;
using System.Windows.Interop;

namespace SmartMouseReceiver;

/// <summary>
/// クリップボード双方向同期
/// </summary>
public static class ClipboardSync
{
    private static IntPtr _hwnd;
    private static HwndSource? _hwndSource;
    private static bool _isProcessingRemote;
    private static string _lastSentText = "";
    private static DateTime _lastSentTime = DateTime.MinValue;

    public static event Action<string>? OnClipboardChanged;

    #region Win32 API

    [DllImport("user32.dll", SetLastError = true)]
    private static extern bool AddClipboardFormatListener(IntPtr hwnd);

    [DllImport("user32.dll", SetLastError = true)]
    private static extern bool RemoveClipboardFormatListener(IntPtr hwnd);

    private const int WM_CLIPBOARDUPDATE = 0x031D;

    #endregion

    /// <summary>
    /// クリップボード監視開始
    /// </summary>
    public static void StartMonitoring(Window window)
    {
        var helper = new WindowInteropHelper(window);
        _hwnd = helper.Handle;

        if (_hwnd == IntPtr.Zero)
        {
            // ウィンドウがまだ作成されていない場合
            window.SourceInitialized += (_, _) =>
            {
                _hwnd = new WindowInteropHelper(window).Handle;
                RegisterListener();
            };
        }
        else
        {
            RegisterListener();
        }
    }

    private static void RegisterListener()
    {
        _hwndSource = HwndSource.FromHwnd(_hwnd);
        _hwndSource?.AddHook(WndProc);
        AddClipboardFormatListener(_hwnd);
    }

    /// <summary>
    /// クリップボード監視停止
    /// </summary>
    public static void StopMonitoring()
    {
        if (_hwnd != IntPtr.Zero)
        {
            RemoveClipboardFormatListener(_hwnd);
            _hwndSource?.RemoveHook(WndProc);
        }
    }

    /// <summary>
    /// ウィンドウプロシージャ
    /// </summary>
    private static IntPtr WndProc(IntPtr hwnd, int msg, IntPtr wParam, IntPtr lParam, ref bool handled)
    {
        if (msg == WM_CLIPBOARDUPDATE)
        {
            // リモートからのコピーは無視（無限ループ防止）
            if (_isProcessingRemote)
            {
                return IntPtr.Zero;
            }

            try
            {
                if (Clipboard.ContainsText())
                {
                    var text = Clipboard.GetText();
                    
                    // 自分が送ったテキストは無視（無限ループ防止）
                    if (text == _lastSentText && 
                        (DateTime.Now - _lastSentTime).TotalSeconds < 2)
                    {
                        return IntPtr.Zero;
                    }

                    OnClipboardChanged?.Invoke(text);
                }
            }
            catch { /* ignore */ }
        }

        return IntPtr.Zero;
    }

    /// <summary>
    /// クリップボードにテキスト設定
    /// </summary>
    public static void SetClipboardText(string text, bool isFromRemote = false)
    {
        try
        {
            _isProcessingRemote = isFromRemote;
            _lastSentText = text;
            _lastSentTime = DateTime.Now;

            // STAスレッドで実行
            if (Application.Current?.Dispatcher != null)
            {
                Application.Current.Dispatcher.Invoke(() =>
                {
                    Clipboard.SetText(text);
                });
            }
        }
        finally
        {
            // 少し遅延してフラグをリセット
            Task.Delay(500).ContinueWith(_ => _isProcessingRemote = false);
        }
    }
}
