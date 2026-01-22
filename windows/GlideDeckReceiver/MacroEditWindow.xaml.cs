using System.Windows;
using System.Windows.Input;

namespace GlideDeckReceiver;

public partial class MacroEditWindow : Window
{
    public MacroItem Macro { get; private set; }
    private bool _isRecording = false;
    private List<VirtualKey> _tempKeys = new List<VirtualKey>();

    public MacroEditWindow(MacroItem? existingMacro = null)
    {
        InitializeComponent();
        
        Loaded += (_, _) => DarkTitleBar.Apply(this);
        
        if (existingMacro != null)
        {
            Macro = existingMacro;
            NameBox.Text = Macro.Name;
            _tempKeys = new List<VirtualKey>(Macro.Keys);
            UpdateKeysDisplay();
        }
        else
        {
            Macro = new MacroItem();
        }

        PreviewKeyDown += OnPreviewKeyDown;
    }

    private void OnPreviewKeyDown(object sender, KeyEventArgs e)
    {
        if (!_isRecording) return;

        e.Handled = true;

        var vk = KeyInterop.VirtualKeyFromKey(e.Key);
        if (e.Key == Key.System) vk = KeyInterop.VirtualKeyFromKey(e.SystemKey);

        var virtualKey = (VirtualKey)vk;

        // Prevent holding down duplicates
        if (_tempKeys.Count > 0 && _tempKeys.Last() == virtualKey) return; 
        
        if (_tempKeys.Count >= 5) return;

        _tempKeys.Add(virtualKey);
        UpdateKeysDisplay();
    }

    private void RecordButton_Click(object sender, RoutedEventArgs e)
    {
        _isRecording = !_isRecording;
        if (_isRecording)
        {
            RecordButton.Content = "⏹ 停止";
            RecordButton.Background = new System.Windows.Media.SolidColorBrush(System.Windows.Media.Color.FromRgb(76, 175, 80)); // Green
            InstructionText.Text = "キーを押してください...";
            InstructionText.Foreground = new System.Windows.Media.SolidColorBrush(System.Windows.Media.Color.FromRgb(76, 175, 80));
            _tempKeys.Clear();
            UpdateKeysDisplay();
            NameBox.IsEnabled = false;
            Focus();
        }
        else
        {
            StopRecording();
        }
    }

    private void StopRecording()
    {
        _isRecording = false;
        RecordButton.Content = "🔴 入力開始";
        RecordButton.Background = new System.Windows.Media.SolidColorBrush(System.Windows.Media.Color.FromRgb(244, 67, 54)); // Red
        InstructionText.Text = "録画ボタンを押してキーを入力";
        InstructionText.Foreground = new System.Windows.Media.SolidColorBrush(System.Windows.Media.Color.FromRgb(128, 128, 128));
        NameBox.IsEnabled = true;
    }

    private void ResetButton_Click(object sender, RoutedEventArgs e)
    {
        _tempKeys.Clear();
        UpdateKeysDisplay();
        if (_isRecording) StopRecording();
    }

    private void UpdateKeysDisplay()
    {
        if (_tempKeys.Count == 0)
        {
            KeysText.Text = "[未設定]";
            return;
        }

        KeysText.Text = string.Join(" + ", _tempKeys.Select(k => k.ToString()));
    }

    private void SaveButton_Click(object sender, RoutedEventArgs e)
    {
        if (string.IsNullOrWhiteSpace(NameBox.Text))
        {
            MessageBox.Show("名前を入力してください");
            return;
        }

        if (_tempKeys.Count == 0)
        {
            MessageBox.Show("キーを割り当ててください");
            return;
        }

        Macro.Name = NameBox.Text;
        Macro.Keys = new List<VirtualKey>(_tempKeys);

        DialogResult = true;
        Close();
    }

    private void CancelButton_Click(object sender, RoutedEventArgs e)
    {
        DialogResult = false;
        Close();
    }
}

