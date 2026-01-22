using System.Windows;

namespace GlideDeckReceiver;

public partial class App : Application
{
    private const string UniqueEventName = "{DEFC06EE-5A5B-409E-9448-C8DF33C4700A}_GlideDeckReceiver";
    private System.Threading.EventWaitHandle? _eventWaitHandle;

    protected override void OnStartup(StartupEventArgs e)
    {
        bool createdNew;
        _eventWaitHandle = new System.Threading.EventWaitHandle(false, System.Threading.EventResetMode.AutoReset, UniqueEventName, out createdNew);

        if (!createdNew)
        {
            // すでに起動しているので、シグナルを送って終了
            _eventWaitHandle.Set();
            Shutdown();
            return;
        }

        // 別スレッドでシグナル監視
        Task.Run(() =>
        {
            while (true)
            {
                _eventWaitHandle.WaitOne();
                Dispatcher.Invoke(() =>
                {
                    var window = Current.MainWindow;
                    if (window != null)
                    {
                        window.Show();
                        window.WindowState = WindowState.Normal;
                        window.Activate();
                        
                        // 最前面に持ってくるための追加処理
                        window.Topmost = true;
                        window.Topmost = false;
                        window.Focus();
                    }
                });
            }
        });

        base.OnStartup(e);
    }

    protected override void OnExit(ExitEventArgs e)
    {
        ClipboardSync.StopMonitoring();
        _eventWaitHandle?.Dispose();
        base.OnExit(e);
    }
}

