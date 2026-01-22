using System;
using System.Collections.Generic;
using System.Drawing;
using System.IO;
using System.Runtime.InteropServices;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Interop;
using System.Windows.Media.Imaging;
using QRCoder;

namespace GlideDeckReceiver;

// Dark Title Bar Support
internal static class DarkTitleBar
{
    [DllImport("dwmapi.dll", PreserveSig = true)]
    private static extern int DwmSetWindowAttribute(IntPtr hwnd, int attr, ref int attrValue, int attrSize);

    private const int DWMWA_USE_IMMERSIVE_DARK_MODE = 20;

    public static void Apply(Window window)
    {
        var hwnd = new WindowInteropHelper(window).Handle;
        if (hwnd == IntPtr.Zero) return;
        int darkMode = 1;
        DwmSetWindowAttribute(hwnd, DWMWA_USE_IMMERSIVE_DARK_MODE, ref darkMode, sizeof(int));
    }
}

public partial class MainWindow : Window
{
    private readonly ServerHost _server;
    private readonly List<NetworkAdapterInfo> _adapters;
    private bool _isEnglish = true;

    public MainWindow()
    {
        InitializeComponent();

        _server = new ServerHost();
        _server.OnLog += message => Dispatcher.Invoke(() => UpdateLog(message));
        _server.OnClientCountChanged += count => Dispatcher.Invoke(() => UpdateClientCount(count));

        // Network Adapters
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
            UpdateStatus(GetString("StatusNoAdapter"), false);
        }

        // Clipboard
        ClipboardSync.StartMonitoring(this);
        ClipboardSync.OnClipboardChanged += OnLocalClipboardChanged;

        Loaded += MainWindow_Loaded;
    }

    private async void MainWindow_Loaded(object sender, RoutedEventArgs e)
    {
        // Apply dark title bar
        DarkTitleBar.Apply(this);
        
        if (_adapters.Count > 0)
        {
            await StartServerAsync(_adapters[0].IpAddress);
        }
        
        RefreshMacroList();
    }

    private void LanguageButton_Click(object sender, RoutedEventArgs e)
    {
        _isEnglish = !_isEnglish;
        SetLanguage(_isEnglish ? "en" : "ja");
        
        // Update button text to show target language (swapped)
        if (sender is Button btn)
        {
             btn.Content = _isEnglish ? "JA" : "EN"; // If English, button shows JA to switch to JA
        }
    }

    private void SetLanguage(string culture)
    {
        var dict = new ResourceDictionary();
        dict.Source = new Uri($"Resources/Languages/Strings.{culture}.xaml", UriKind.Relative);

        App.Current.Resources.MergedDictionaries.Clear();
        App.Current.Resources.MergedDictionaries.Add(dict);
        
        // Refresh dynamic UI elements
        if (_server.IsRunning)
            UpdateStatus(GetString("StatusServerRunning"), true);
        else
            UpdateStatus(GetString("StatusNoAdapter"), false);
            
        UpdateClientCount(_server.ClientCount);
    }

    private string GetString(string key)
    {
        return Application.Current.FindResource(key) as string ?? key;
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
            UpdateStatus(GetString("StatusServerRunning"), true);
        }
        catch (Exception ex)
        {
            UpdateStatus($"Error: {ex.Message}", false);
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
            isOk ? System.Windows.Media.Color.FromRgb(52, 199, 89)   // Green
                 : System.Windows.Media.Color.FromRgb(255, 59, 48)); // Red
    }

    private void UpdateClientCount(int count)
    {
        string format = GetString("LabelClients");
        ClientCountText.Text = $"{format}{count}";
    }

    private void UpdateLog(string message)
    {
        System.Diagnostics.Debug.WriteLine(message);
    }

    private async void OnLocalClipboardChanged(string text)
    {
        await _server.BroadcastClipboardAsync(text);
    }

    private async void IpComboBox_SelectionChanged(object sender, SelectionChangedEventArgs e)
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
                _server.MacroManager.Save();
                RefreshMacroList();
            }
        }
    }

    private void DeleteMacro_Click(object sender, RoutedEventArgs e)
    {
        if (MacroList.SelectedItem is MacroItem selected)
        {
            var msgFormat = GetString("MsgDeleteConfirm");
            var title = GetString("TitleConfirm");
            
            if (MessageBox.Show(string.Format(msgFormat, selected.Name), title, MessageBoxButton.YesNo) == MessageBoxResult.Yes)
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

    private void Window_MouseLeftButtonDown(object sender, MouseButtonEventArgs e)
    {
        if (e.ButtonState == MouseButtonState.Pressed)
        {
            DragMove();
        }
    }
}
