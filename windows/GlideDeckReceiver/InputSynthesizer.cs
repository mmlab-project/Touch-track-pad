using System.Runtime.InteropServices;

namespace GlideDeckReceiver;

/// <summary>
/// Win32 SendInput APIによる入力シンセサイザー
/// </summary>
public static class InputSynthesizer
{
    #region Win32 API Definitions

    [DllImport("user32.dll", SetLastError = true)]
    private static extern uint SendInput(uint nInputs, INPUT[] pInputs, int cbSize);

    [DllImport("user32.dll")]
    private static extern short VkKeyScan(char ch);

    [DllImport("user32.dll")]
    private static extern int GetSystemMetrics(int nIndex);

    private const int SM_CXSCREEN = 0;
    private const int SM_CYSCREEN = 1;

    [StructLayout(LayoutKind.Sequential)]
    private struct INPUT
    {
        public uint type;
        public InputUnion U;
    }

    [StructLayout(LayoutKind.Explicit)]
    private struct InputUnion
    {
        [FieldOffset(0)] public MOUSEINPUT mi;
        [FieldOffset(0)] public KEYBDINPUT ki;
    }

    [StructLayout(LayoutKind.Sequential)]
    private struct MOUSEINPUT
    {
        public int dx;
        public int dy;
        public int mouseData;
        public uint dwFlags;
        public uint time;
        public IntPtr dwExtraInfo;
    }

    [StructLayout(LayoutKind.Sequential)]
    private struct KEYBDINPUT
    {
        public ushort wVk;
        public ushort wScan;
        public uint dwFlags;
        public uint time;
        public IntPtr dwExtraInfo;
    }

    private const uint INPUT_MOUSE = 0;
    private const uint INPUT_KEYBOARD = 1;

    private const uint MOUSEEVENTF_MOVE = 0x0001;
    private const uint MOUSEEVENTF_LEFTDOWN = 0x0002;
    private const uint MOUSEEVENTF_LEFTUP = 0x0004;
    private const uint MOUSEEVENTF_RIGHTDOWN = 0x0008;
    private const uint MOUSEEVENTF_RIGHTUP = 0x0010;
    private const uint MOUSEEVENTF_MIDDLEDOWN = 0x0020;
    private const uint MOUSEEVENTF_MIDDLEUP = 0x0040;
    private const uint MOUSEEVENTF_WHEEL = 0x0800;
    private const uint MOUSEEVENTF_HWHEEL = 0x1000;

    private const uint KEYEVENTF_EXTENDEDKEY = 0x0001;
    private const uint KEYEVENTF_KEYUP = 0x0002;
    private const uint KEYEVENTF_UNICODE = 0x0004;

    #endregion

    #region Mouse Operations

    /// <summary>
    /// マウス相対移動
    /// </summary>
    public static void MoveMouse(int dx, int dy)
    {
        var input = new INPUT
        {
            type = INPUT_MOUSE,
            U = new InputUnion
            {
                mi = new MOUSEINPUT
                {
                    dx = dx,
                    dy = dy,
                    dwFlags = MOUSEEVENTF_MOVE
                }
            }
        };
        SendInput(1, [input], Marshal.SizeOf<INPUT>());
    }

    /// <summary>
    /// マウスクリック
    /// </summary>
    public static void MouseClick(MouseButton button)
    {
        var (downFlag, upFlag) = button switch
        {
            MouseButton.Left => (MOUSEEVENTF_LEFTDOWN, MOUSEEVENTF_LEFTUP),
            MouseButton.Right => (MOUSEEVENTF_RIGHTDOWN, MOUSEEVENTF_RIGHTUP),
            MouseButton.Middle => (MOUSEEVENTF_MIDDLEDOWN, MOUSEEVENTF_MIDDLEUP),
            _ => (MOUSEEVENTF_LEFTDOWN, MOUSEEVENTF_LEFTUP)
        };

        var inputs = new INPUT[]
        {
            new() { type = INPUT_MOUSE, U = new InputUnion { mi = new MOUSEINPUT { dwFlags = downFlag } } },
            new() { type = INPUT_MOUSE, U = new InputUnion { mi = new MOUSEINPUT { dwFlags = upFlag } } }
        };
        SendInput(2, inputs, Marshal.SizeOf<INPUT>());
    }

    /// <summary>
    /// マウスボタン押下
    /// </summary>
    public static void MouseDown(MouseButton button)
    {
        var flag = button switch
        {
            MouseButton.Left => MOUSEEVENTF_LEFTDOWN,
            MouseButton.Right => MOUSEEVENTF_RIGHTDOWN,
            MouseButton.Middle => MOUSEEVENTF_MIDDLEDOWN,
            _ => MOUSEEVENTF_LEFTDOWN
        };

        var input = new INPUT
        {
            type = INPUT_MOUSE,
            U = new InputUnion { mi = new MOUSEINPUT { dwFlags = flag } }
        };
        SendInput(1, [input], Marshal.SizeOf<INPUT>());
    }

    /// <summary>
    /// マウスボタン解放
    /// </summary>
    public static void MouseUp(MouseButton button)
    {
        var flag = button switch
        {
            MouseButton.Left => MOUSEEVENTF_LEFTUP,
            MouseButton.Right => MOUSEEVENTF_RIGHTUP,
            MouseButton.Middle => MOUSEEVENTF_MIDDLEUP,
            _ => MOUSEEVENTF_LEFTUP
        };

        var input = new INPUT
        {
            type = INPUT_MOUSE,
            U = new InputUnion { mi = new MOUSEINPUT { dwFlags = flag } }
        };
        SendInput(1, [input], Marshal.SizeOf<INPUT>());
    }

    /// <summary>
    /// マウスホイールスクロール（垂直）
    /// </summary>
    public static void MouseWheel(int delta)
    {
        var input = new INPUT
        {
            type = INPUT_MOUSE,
            U = new InputUnion
            {
                mi = new MOUSEINPUT
                {
                    mouseData = delta * 120, // WHEEL_DELTA = 120
                    dwFlags = MOUSEEVENTF_WHEEL
                }
            }
        };
        SendInput(1, [input], Marshal.SizeOf<INPUT>());
    }

    /// <summary>
    /// マウスホイールスクロール（水平）
    /// </summary>
    public static void MouseHWheel(int delta)
    {
        var input = new INPUT
        {
            type = INPUT_MOUSE,
            U = new InputUnion
            {
                mi = new MOUSEINPUT
                {
                    mouseData = delta * 120,
                    dwFlags = MOUSEEVENTF_HWHEEL
                }
            }
        };
        SendInput(1, [input], Marshal.SizeOf<INPUT>());
    }

    #endregion

    #region Keyboard Operations

    /// <summary>
    /// Unicode文字入力
    /// </summary>
    public static void TypeCharacter(char c)
    {
        var inputs = new INPUT[]
        {
            new()
            {
                type = INPUT_KEYBOARD,
                U = new InputUnion
                {
                    ki = new KEYBDINPUT
                    {
                        wScan = c,
                        dwFlags = KEYEVENTF_UNICODE
                    }
                }
            },
            new()
            {
                type = INPUT_KEYBOARD,
                U = new InputUnion
                {
                    ki = new KEYBDINPUT
                    {
                        wScan = c,
                        dwFlags = KEYEVENTF_UNICODE | KEYEVENTF_KEYUP
                    }
                }
            }
        };
        SendInput(2, inputs, Marshal.SizeOf<INPUT>());
    }

    /// <summary>
    /// 文字列入力
    /// </summary>
    public static void TypeString(string text)
    {
        foreach (var c in text)
        {
            TypeCharacter(c);
        }
    }

    /// <summary>
    /// 仮想キー押下
    /// </summary>
    public static void KeyDown(VirtualKey key)
    {
        var input = new INPUT
        {
            type = INPUT_KEYBOARD,
            U = new InputUnion
            {
                ki = new KEYBDINPUT
                {
                    wVk = (ushort)key,
                    dwFlags = IsExtendedKey(key) ? KEYEVENTF_EXTENDEDKEY : 0
                }
            }
        };
        SendInput(1, [input], Marshal.SizeOf<INPUT>());
    }

    /// <summary>
    /// 仮想キー解放
    /// </summary>
    public static void KeyUp(VirtualKey key)
    {
        var input = new INPUT
        {
            type = INPUT_KEYBOARD,
            U = new InputUnion
            {
                ki = new KEYBDINPUT
                {
                    wVk = (ushort)key,
                    dwFlags = KEYEVENTF_KEYUP | (IsExtendedKey(key) ? KEYEVENTF_EXTENDEDKEY : 0)
                }
            }
        };
        SendInput(1, [input], Marshal.SizeOf<INPUT>());
    }

    /// <summary>
    /// 仮想キー押下→解放
    /// </summary>
    public static void KeyPress(VirtualKey key)
    {
        KeyDown(key);
        KeyUp(key);
    }

    /// <summary>
    /// 修飾キー付きキー入力
    /// </summary>
    public static void KeyPressWithModifiers(VirtualKey key, params VirtualKey[] modifiers)
    {
        // 修飾キー押下
        foreach (var mod in modifiers)
        {
            KeyDown(mod);
        }

        // メインキー
        KeyPress(key);

        // 修飾キー解放（逆順）
        foreach (var mod in modifiers.Reverse())
        {
            KeyUp(mod);
        }
    }

    /// <summary>
    /// 拡張キー判定
    /// </summary>
    private static bool IsExtendedKey(VirtualKey key)
    {
        return key switch
        {
            VirtualKey.Insert or VirtualKey.Delete or VirtualKey.Home or
            VirtualKey.End or VirtualKey.PageUp or VirtualKey.PageDown or
            VirtualKey.Left or VirtualKey.Right or VirtualKey.Up or VirtualKey.Down or
            VirtualKey.NumLock or VirtualKey.PrintScreen or VirtualKey.RMenu or
            VirtualKey.RControl => true,
            _ => false
        };
    }

    #endregion
}

public enum MouseButton
{
    Left,
    Right,
    Middle,
    Button4,
    Button5
}

public enum VirtualKey : ushort
{
    // 修飾キー
    LShift = 0xA0,
    RShift = 0xA1,
    LControl = 0xA2,
    RControl = 0xA3,
    LMenu = 0xA4,  // Left Alt
    RMenu = 0xA5,  // Right Alt
    LWin = 0x5B,
    RWin = 0x5C,

    // ファンクションキー
    F1 = 0x70, F2 = 0x71, F3 = 0x72, F4 = 0x73,
    F5 = 0x74, F6 = 0x75, F7 = 0x76, F8 = 0x77,
    F9 = 0x78, F10 = 0x79, F11 = 0x7A, F12 = 0x7B,

    // 特殊キー
    Escape = 0x1B,
    Tab = 0x09,
    CapsLock = 0x14,
    Space = 0x20,
    Enter = 0x0D,
    Backspace = 0x08,
    Delete = 0x2E,
    Insert = 0x2D,
    Home = 0x24,
    End = 0x23,
    PageUp = 0x21,
    PageDown = 0x22,
    PrintScreen = 0x2C,
    ScrollLock = 0x91,
    Pause = 0x13,
    NumLock = 0x90,

    // 矢印キー
    Left = 0x25,
    Up = 0x26,
    Right = 0x27,
    Down = 0x28,

    // 数字キー
    D0 = 0x30, D1 = 0x31, D2 = 0x32, D3 = 0x33, D4 = 0x34,
    D5 = 0x35, D6 = 0x36, D7 = 0x37, D8 = 0x38, D9 = 0x39,

    // アルファベット
    A = 0x41, B = 0x42, C = 0x43, D = 0x44, E = 0x45,
    F = 0x46, G = 0x47, H = 0x48, I = 0x49, J = 0x4A,
    K = 0x4B, L = 0x4C, M = 0x4D, N = 0x4E, O = 0x4F,
    P = 0x50, Q = 0x51, R = 0x52, S = 0x53, T = 0x54,
    U = 0x55, V = 0x56, W = 0x57, X = 0x58, Y = 0x59,
    Z = 0x5A,

    // 記号
    OemSemicolon = 0xBA,
    OemPlus = 0xBB,
    OemComma = 0xBC,
    OemMinus = 0xBD,
    OemPeriod = 0xBE,
    OemQuestion = 0xBF,
    OemTilde = 0xC0,
    OemOpenBrackets = 0xDB,
    OemPipe = 0xDC,
    OemCloseBrackets = 0xDD,
    OemQuotes = 0xDE,

    // メディアキー
    VolumeMute = 0xAD,
    VolumeDown = 0xAE,
    VolumeUp = 0xAF,
    MediaNext = 0xB0,
    MediaPrev = 0xB1,
    MediaStop = 0xB2,
    MediaPlayPause = 0xB3,
    
    // IME
    Kanji = 0x19, // 半角/全角
    Convert = 0x1C, // 変換
    NonConvert = 0x1D, // 無変換
    Kana = 0x15 // カナ
}

