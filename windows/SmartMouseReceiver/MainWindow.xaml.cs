using System.Drawing;
using System.IO;
using System.Windows;
using System.Windows.Media.Imaging;
using QRCoder;

namespace SmartMouseReceiver;

public partial class MainWindow : Window
{
    private readonly ServerHost _server;
    private readonly List<NetworkAdapterInfo> _adapters;

    public MainWindow()
    {
        InitializeComponent();

        _server = new ServerHost();
        _server.OnLog += message => Dispatcher.Invoke(() => UpdateLog(message));
        _server.OnClientCountChanged += count => Dispatcher.Invoke(() => UpdateClientCount(count));

        // ネットワークアダプタ一覧取得
        _adapters = NetworkUtils.GetAvailableNetworkAdapters();
        foreach (var adapter in _adapters)
        {
            IpComboBox.Items.Add(adapter.ToString());
        }

        if (_adapters.Count > 0)
        {
            IpComboBox.SelectedIndex = 0;
        }
        else
        {
            StatusText.Text = "ネットワークアダプタが見つかりません";
            StatusIndicator.Fill = new System.Windows.Media.SolidColorBrush(
                System.Windows.Media.Color.FromRgb(244, 67, 54));
        }

        // クリップボード同期設定
        ClipboardSync.StartMonitoring(this);
        ClipboardSync.OnClipboardChanged += OnLocalClipboardChanged;

        Loaded += MainWindow_Loaded;
    }

    private async void MainWindow_Loaded(object sender, RoutedEventArgs e)
    {
        if (_adapters.Count > 0)
        {
            await StartServerAsync(_adapters[0].IpAddress);
        }
        
        RefreshMacroList();
    }

    private async Task StartServerAsync(string ip)
    {
        try
        {
            _server.Stop();
            await _server.StartAsync(ip);
            
            IpText.Text = _server.CurrentIp;
            PortText.Text = _server.Port.ToString();
            
            UpdateQrCode();
            UpdateStatus("サーバー稼働中", true);
        }
        catch (Exception ex)
        {
            UpdateStatus($"エラー: {ex.Message}", false);
        }
    }

    private void UpdateQrCode()
    {
        var qrJson = _server.GetQrCodeJson();
        
        using var qrGenerator = new QRCodeGenerator();
        using var qrCodeData = qrGenerator.CreateQrCode(qrJson, QRCodeGenerator.ECCLevel.M);
        using var qrCode = new QRCode(qrCodeData);
        using var bitmap = qrCode.GetGraphic(10, Color.Black, Color.White, true);

        QrCodeImage.Source = BitmapToImageSource(bitmap);
    }

    private static BitmapImage BitmapToImageSource(Bitmap bitmap)
    {
        using var memory = new MemoryStream();
        bitmap.Save(memory, System.Drawing.Imaging.ImageFormat.Png);
        memory.Position = 0;
        
        var bitmapImage = new BitmapImage();
        bitmapImage.BeginInit();
        bitmapImage.StreamSource = memory;
        bitmapImage.CacheOption = BitmapCacheOption.OnLoad;
        bitmapImage.EndInit();
        bitmapImage.Freeze();
        
        return bitmapImage;
    }

    private void UpdateStatus(string text, bool isOk)
    {
        StatusText.Text = text;
        StatusIndicator.Fill = new System.Windows.Media.SolidColorBrush(
            isOk ? System.Windows.Media.Color.FromRgb(76, 175, 80)  // Green
                 : System.Windows.Media.Color.FromRgb(244, 67, 54)); // Red
    }

    private void UpdateClientCount(int count)
    {
        ClientCountText.Text = $"接続クライアント: {count}";
    }

    private void UpdateLog(string message)
    {
        System.Diagnostics.Debug.WriteLine(message);
    }

    private async void OnLocalClipboardChanged(string text)
    {
        await _server.BroadcastClipboardAsync(text);
    }

    private async void IpComboBox_SelectionChanged(object sender, System.Windows.Controls.SelectionChangedEventArgs e)
    {
        if (IpComboBox.SelectedIndex >= 0 && IpComboBox.SelectedIndex < _adapters.Count)
        {
            await StartServerAsync(_adapters[IpComboBox.SelectedIndex].IpAddress);
        }
    }

    private void RefreshButton_Click(object sender, RoutedEventArgs e)
    {
        if (_server.IsRunning)
        {
            // 新しいトークンでQR再生成
            _ = StartServerAsync(_server.CurrentIp);
        }
    }

    private void RefreshMacroList()
    {
        MacroList.ItemsSource = null;
        MacroList.ItemsSource = _server.MacroManager.Macros;
    }

    private void AddMacro_Click(object sender, RoutedEventArgs e)
    {
        var dialog = new MacroEditWindow();
        dialog.Owner = this;
        if (dialog.ShowDialog() == true)
        {
            _server.MacroManager.AddMacro(dialog.Macro);
            RefreshMacroList();
        }
    }

    private void EditMacro_Click(object sender, RoutedEventArgs e)
    {
        if (MacroList.SelectedItem is MacroItem selected)
        {
            var dialog = new MacroEditWindow(selected);
            dialog.Owner = this;
            if (dialog.ShowDialog() == true)
            {
                // Macro object is updated by reference, just save
                _server.MacroManager.Save();
                RefreshMacroList();
            }
        }
    }

    private void DeleteMacro_Click(object sender, RoutedEventArgs e)
    {
        if (MacroList.SelectedItem is MacroItem selected)
        {
            if (MessageBox.Show($"マクロ '{selected.Name}' を削除しますか？", "確認", MessageBoxButton.YesNo) == MessageBoxResult.Yes)
            {
                _server.MacroManager.RemoveMacro(selected.Id);
                RefreshMacroList();
            }
        }
    }

    private void MinimizeButton_Click(object sender, RoutedEventArgs e)
    {
        Hide();
    }

    private void TrayIcon_TrayMouseDoubleClick(object sender, RoutedEventArgs e)
    {
        Show();
        WindowState = WindowState.Normal;
        Activate();
    }

    private void MenuItem_Show_Click(object sender, RoutedEventArgs e)
    {
        Show();
        WindowState = WindowState.Normal;
        Activate();
    }

    private void MenuItem_Exit_Click(object sender, RoutedEventArgs e)
    {
        _server.Dispose();
        TrayIcon.Dispose();
        Application.Current.Shutdown();
    }

    private void Window_Closing(object sender, System.ComponentModel.CancelEventArgs e)
    {
        // 閉じるボタンでトレイに格納
        e.Cancel = true;
        Hide();
    }

    private void Window_StateChanged(object sender, EventArgs e)
    {
        if (WindowState == WindowState.Minimized)
        {
            Hide();
        }
    }
}
