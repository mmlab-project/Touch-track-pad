using System.IO;
using System.Threading.Tasks;
using System.Windows;

namespace GlideDeckReceiver;

public partial class App : Application
{
    private const string UniqueEventName = "{DEFC06EE-5A5B-409E-9448-C8DF33C4700A}_GlideDeckReceiver";
    private System.Threading.EventWaitHandle? _eventWaitHandle;

    public App()
    {
        // Global Exception Handling - Subscribe EARLY in constructor
        AppDomain.CurrentDomain.UnhandledException += (s, args) => LogException(args.ExceptionObject as Exception, "AppDomain");
        DispatcherUnhandledException += (s, args) => 
        {
            LogException(args.Exception, "Dispatcher");
            args.Handled = true; 
        };
    }

    protected override void OnStartup(StartupEventArgs e)
    {
        bool createdNew;
        _eventWaitHandle = new System.Threading.EventWaitHandle(false, System.Threading.EventResetMode.AutoReset, UniqueEventName, out createdNew);

        if (!createdNew)
        {
            _eventWaitHandle.Set();
            Shutdown();
            return;
        }

        Task.Run(() =>
        {
            try
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
                            window.Topmost = true;
                            window.Topmost = false;
                            window.Focus();
                        }
                    });
                }
            }
            catch (Exception ex)
            {
                LogException(ex, "SignalWatcher");
            }
        });

        base.OnStartup(e);
    }

    private static void LogException(Exception? ex, string source)
    {
        if (ex == null) return;
        try
        {
            string logPath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "crash_log.txt");
            string message = $"[{DateTime.Now}] [{source}] Error: {ex.Message}\nStack Trace: {ex.StackTrace}\n\n";
            File.AppendAllText(logPath, message);
            MessageBox.Show($"Startup Error: {ex.Message}", "GlideDeck Error", MessageBoxButton.OK, MessageBoxImage.Error);
        }
        catch { /* Best effort logging */ }
    }

    protected override void OnExit(ExitEventArgs e)
    {
        ClipboardSync.StopMonitoring();
        _eventWaitHandle?.Dispose();
        base.OnExit(e);
    }
}

