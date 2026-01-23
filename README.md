<div id="top"></div>

<p align="center">
  <img src="docs/icon.png" alt="GlideDeck Icon" width="128" height="128">
</p>
<h1 align="center">GlideDeck</h1>
<p align="center"><em>スマホがトラックパッドに変わる</em></p>

## 使用技術一覧

<!-- シールド一覧 -->
<p style="display: inline">
  <!-- Android -->
  <img src="https://img.shields.io/badge/-Android-3DDC84.svg?logo=android&style=for-the-badge&logoColor=white">
  <img src="https://img.shields.io/badge/-Kotlin-7F52FF.svg?logo=kotlin&style=for-the-badge&logoColor=white">
  <img src="https://img.shields.io/badge/-Jetpack%20Compose-4285F4.svg?logo=jetpackcompose&style=for-the-badge&logoColor=white">
  <!-- Windows -->
  <img src="https://img.shields.io/badge/-.NET-512BD4.svg?logo=dotnet&style=for-the-badge&logoColor=white">
  <img src="https://img.shields.io/badge/-C%23-239120.svg?logo=csharp&style=for-the-badge&logoColor=white">
  <img src="https://img.shields.io/badge/-WPF-0078D4.svg?logo=windows&style=for-the-badge&logoColor=white">
  <!-- その他 -->
  <img src="https://img.shields.io/badge/-Wi--Fi-0088CC.svg?logo=wifi&style=for-the-badge&logoColor=white">
  <img src="https://img.shields.io/badge/-QR%20Code-41BDF5.svg?logo=qrcode&style=for-the-badge&logoColor=white">
</p>

<!-- ステータスバッジ -->
<p align="center">
  <img src="https://img.shields.io/badge/version-1.1.0-blue.svg?cacheSeconds=2592000" />
  <img src="https://img.shields.io/badge/Android-8.0%2B-green.svg" />
  <img src="https://img.shields.io/badge/.NET-8.0-purple.svg" />
  <img src="https://img.shields.io/badge/PRs-welcome-brightgreen.svg" />
  <img src="https://img.shields.io/badge/Maintained%3F-yes-green.svg" />
</p>

## 目次

1. [プロジェクトについて](#プロジェクトについて)
2. [デモ](#デモ)
3. [主な機能](#主な機能)
4. [環境](#環境)
5. [ディレクトリ構成](#ディレクトリ構成)
6. [セットアップ](#セットアップ)
7. [使い方](#使い方)
8. [開発環境構築](#開発環境構築)
9. [トラブルシューティング](#トラブルシューティング)
10. [コントリビューション](#コントリビューション)
11. [ライセンス](#ライセンス)
12. [作者](#作者)
13. [謝辞](#謝辞)

<br />

<!-- プロジェクト名を記載 -->

# 📱 GlideDeck

> **AndroidスマートフォンをWindows PCの高性能トラックパッド・キーボード・マクロコントローラーに変える**

外出先でマウスがない？プレゼンでリモコンが必要？複雑な操作を効率化したい？  
GlideDeckがあれば、お手持ちのスマホが最高の入力デバイスに早変わり！

## 🎥 デモ

> [!NOTE]
> デモGIFやスクリーンショットは今後追加予定です。アプリには以下の画面が含まれています：
> - QRコード接続画面
> - トラックパッド操作画面
> - キーボード入力画面
> - マクロ実行画面

<!-- デモGIFを追加する場合はここに配置 -->
<!-- ![SmartMouse Demo](docs/demo.gif) -->

### ✨ 主な特徴

```
🖱️ Magic Trackpadライクな操作感    ⌨️ フルキーボード対応
🎯 カスタムマクロ機能              📱 QRコード簡単接続
⚡ 低遅延Wi-Fi通信                 🎨 Material Design 3
```

<!-- プロジェクトについて -->

## プロジェクトについて

GlideDeckは、お手持ちのAndroidスマートフォンをWindows PCの入力デバイスとして活用できる革新的なアプリケーションです。Wi-Fi経由で接続し、スマホの画面をトラックパッドやキーボードとして使用できます。

### 💡 開発の背景

このプロジェクトは、以下のような背景から開発されました：

- **💸 トラックパッドは高い**: Magic Trackpadなど高性能トラックパッドは高価だが、指の数と動作で機能が増える点でマウスより便利
- **📱 スマホを活用**: 手元に常にあるスマホをトラックパッド代わりに使えないか？
- **🤖 AI時代の開発**: Vibe CodingでAIと協力して試行錯誤の末、この形に落ち着いた
- **🔋 マウス不要**: ワイヤレスマウスの充電忘れの心配なし、スマホを充電しておけばOK

**実際に試して評価してくれたらうれしいです！**

### 🎯 プロジェクトの目標

- [x] Wi-Fi経由での低遅延通信の実現
- [x] Magic Trackpadと同等のジェスチャー対応
- [x] 直感的なUI/UXの提供
- [x] QRコードによる簡単接続
- [x] カスタムマクロ機能の実装
- [ ] Bluetooth接続対応（今後の予定）
- [ ] macOS/Linux対応（今後の予定）

<p align="right">(<a href="#top">トップへ</a>)</p>

## 主な機能

### 🖱️ 高精度トラックパッド
- **Magic Trackpadライクな操作感**: スムーズなカーソル移動と精密な操作
- **マルチタッチジェスチャー対応**:
  - 1本指: カーソル移動、タップでクリック、**ダブルタップ＆ホールドでドラッグ**
  - 2本指: スクロール、右クリック、水平スワイプ（戻る/進む）、**長押しでメニュー表示**
  - 3本指: ミドルクリック、ピンチイン（デスクトップ表示）、スワイプ（Alt+Tab）
  - 4本指: スワイプ（タスクビュー）
- **感度調整**: マウス感度とスクロール感度を個別に調整可能
- **スクロール方向**: 標準/リバース（macOS風）を選択可能

### ⌨️ フルキーボード入力
- **完全なキーボードレイアウト**: 標準的なQWERTY配列
- **ショートカットキー**: Ctrl、Alt、Shift、Winキーの組み合わせに対応
- **日本語入力**: スマホのIMEを活用した快適な文字入力

### 🎯 マクロ機能
- **カスタムマクロ**: よく使うキー操作を登録
- **ワンタップ実行**: 複雑な操作を1回のタップで実行
- **マクロ管理**: 追加・編集・削除が簡単

### 📱 QRコード接続
- **複雑な設定不要**: QRコードをスキャンするだけで即座に接続
- **自動ネットワーク設定**: IPアドレスやポート番号の手動入力不要
- **セキュア接続**: 同一ネットワーク内でのみ動作

### 🎨 プレミアムUI/UX
- **Material Design 3**: 最新のデザインガイドラインに準拠
- **ダーク/ライトモード対応**: 自動切り替えまたは手動選択
- **多言語対応**: 日本語・英語のUI切り替え
- **レスポンシブ設定画面**: 様々な画面サイズに最適化
- **触覚フィードバック**: 操作感を高めるバイブレーション

<p align="right">(<a href="#top">トップへ</a>)</p>

## 環境

<!-- 言語、フレームワーク、ミドルウェア、インフラの一覧とバージョンを記載 -->

### Windows側 (Receiver)

| 技術スタック | バージョン |
| ------------ | ---------- |
| .NET         | 8.0        |
| C#           | 12.0       |
| WPF          | -          |
| QRCoder      | 1.4.3      |
| Newtonsoft.Json | 13.0.3  |

### Android側 (Sender)

| 技術スタック | バージョン |
| ------------ | ---------- |
| Kotlin       | 1.9.22     |
| Android SDK  | 34 (Target) / 26 (Min) |
| Jetpack Compose | 2024.02.00 |
| Material3    | Latest     |
| CameraX      | 1.3.1      |
| ML Kit       | 17.2.0     |
| Navigation Compose | 2.7.7 |
| DataStore    | 1.0.0      |

### ネットワーク要件

- PCとスマートフォンが同じWi-Fiネットワークに接続されていること
- ファイアウォールでポート `12345` (デフォルト) が許可されていること

<p align="right">(<a href="#top">トップへ</a>)</p>

## ディレクトリ構成

```
smartmause/
├── android/                          # Androidアプリケーション
│   ├── app/
│   │   ├── src/
│   │   │   └── main/
│   │   │       ├── java/com/glidedeck/infinity/
│   │   │       │   ├── MainActivity.kt          # メインアクティビティ
│   │   │       │   ├── MainViewModel.kt         # メインビューモデル
│   │   │       │   ├── data/
│   │   │       │   │   └── SettingsRepository.kt # 設定管理
│   │   │       │   ├── network/
│   │   │       │   │   ├── NetworkClient.kt     # ネットワーク通信
│   │   │       │   │   └── Macro.kt             # マクロデータモデル
│   │   │       │   ├── ui/
│   │   │       │   │   ├── TrackpadScreen.kt    # トラックパッド画面
│   │   │       │   │   ├── KeyboardScreen.kt    # キーボード画面
│   │   │       │   │   ├── MacroScreen.kt       # マクロ画面
│   │   │       │   │   ├── QrScannerScreen.kt   # QRスキャナー画面
│   │   │       │   │   └── theme/               # テーマ設定
│   │   │       │   └── util/
│   │   │       │       ├── HapticManager.kt     # 触覚フィードバック
│   │   │       │       └── Localization.kt      # ローカライゼーション
│   │   │       ├── res/                         # リソースファイル
│   │   │       └── AndroidManifest.xml
│   │   └── build.gradle.kts                     # ビルド設定
│   ├── build.gradle.kts                         # プロジェクトレベルビルド設定
│   └── settings.gradle.kts
│
├── windows/                          # Windowsアプリケーション
│   └── GlideDeckReceiver/
│       ├── App.xaml                             # アプリケーション定義
│       ├── App.xaml.cs
│       ├── MainWindow.xaml                      # メインウィンドウUI
│       ├── MainWindow.xaml.cs                   # メインウィンドウロジック
│       ├── MacroEditWindow.xaml                 # マクロ編集ウィンドウUI
│       ├── MacroEditWindow.xaml.cs              # マクロ編集ロジック
│       ├── ServerHost.cs                        # サーバーホスト
│       ├── ClientSession.cs                     # クライアントセッション管理
│       ├── InputSynthesizer.cs                  # 入力シミュレーション
│       ├── MacroManager.cs                      # マクロ管理
│       ├── ClipboardSync.cs                     # クリップボード同期
│       ├── NetworkUtils.cs                      # ネットワークユーティリティ
│       ├── GlideDeckReceiver.csproj             # プロジェクトファイル
│       ├── icon.ico                             # アプリケーションアイコン
│       └── macros.json                          # マクロ保存ファイル
│
├── .gitignore
└── README.md
```

<p align="right">(<a href="#top">トップへ</a>)</p>

## セットアップ

### Windows側 (Receiver)

#### 方法1: リリース版を使用（推奨）

1. [Releasesページ](https://github.com/mmlab-project/Touch-track-pad/releases) から最新版の `GlideDeckReceiver.exe` をダウンロード
2. ダウンロードしたファイルを任意の場所に配置
3. `GlideDeckReceiver.exe` を実行

> [!IMPORTANT]
> 初回起動時、Windowsファイアウォールの警告が表示される場合があります。「アクセスを許可する」を選択してください。

#### 方法2: ソースからビルド

```powershell
# リポジトリをクローン
git clone https://github.com/mmlab-project/Touch-track-pad.git
cd Touch-track-pad/windows/GlideDeckReceiver

# ビルド
dotnet build -c Release

# 実行
dotnet run
dotnet run
```

#### Windowsインストーラーの作成

配布用のインストーラー（Setupファイル）を作成するには、[Inno Setup](https://jrsoftware.org/isdl.php) を使用します。

1. **Inno Setup** をインストール
2. `windows/GlideDeckReceiver/SetupScript/setup.iss` をダブルクリックして開く
3. ツールバーの **Build** ボタン（または `Ctrl + F9`）をクリック
4. 以下のフォルダにインストーラーが生成されます：
   ```
   windows/GlideDeckReceiver/SetupScript/Output/GlideDeckReceiver_Setup.exe
   ```

### Android側 (Sender)

#### 方法1: APKをインストール（推奨）

1. [Releasesページ](https://github.com/mmlab-project/Touch-track-pad/releases) から最新版の `GlideDeck.apk` をダウンロード
2. スマートフォンにAPKをインストール
3. アプリを起動

> [!WARNING]
> 提供元不明のアプリのインストールを許可する必要があります。

#### 方法2: Android Studioでビルド

```bash
# リポジトリをクローン
git clone https://github.com/mmlab-project/Touch-track-pad.git

# Android Studioで android ディレクトリを開く
# ビルドして実機またはエミュレータで実行
```

<p align="right">(<a href="#top">トップへ</a>)</p>

## 使い方

### 🚀 初回接続（3ステップで完了！）

<details open>
<summary><b>ステップ1: Windows側の準備</b></summary>

1. `GlideDeckReceiver.exe` を起動
2. メインウィンドウにQRコードが自動表示されます

![QRコード表示画面のイメージ]
```
┌─────────────────────────┐
│  GlideDeck Receiver      │
├─────────────────────────┤
│                         │
│     ███████████████     │
│     ███████████████     │  ← このQRコードを
│     ███████████████     │    スマホでスキャン
│     ███████████████     │
│                         │
│  接続待機中...          │
└─────────────────────────┘
```

</details>

<details open>
<summary><b>ステップ2: Android側の準備</b></summary>

1. GlideDeckアプリを起動
2. カメラ権限を許可（初回のみ）
3. QRスキャナー画面が自動的に表示されます

</details>

<details open>
<summary><b>ステップ3: 接続</b></summary>

1. スマホのカメラでWindows側のQRコードをスキャン
2. 自動的に接続が確立されます（約1-2秒）
3. トラックパッド画面に自動遷移 ✨

> [!TIP]
> 一度接続すれば、次回からはQRコードスキャン不要で自動接続されます！

</details>

---

### 🖱️ トラックパッドの使い方

#### 基本操作

| ジェスチャー | 動作 | 用途 |
| ------------ | ---- | ---- |
| 1本指移動 | カーソル移動 | 通常のマウス操作 |
| 1本指タップ | 左クリック | ファイル選択、リンククリック |
| 1本指ダブルタップ＆ホールド | **ドラッグ開始** | テキスト選択、ファイル移動 |
| 2本指タップ | 右クリック | コンテキストメニュー |
| 2本指長押し | メニュー表示 | アプリ内メニュー |
| 3本指タップ | 中クリック | リンクを新しいタブで開く |

#### スクロール

| ジェスチャー | 動作 | 用途 |
| ------------ | ---- | ---- |
| 2本指縦スクロール | 上下スクロール | Webページ、ドキュメント |

#### 高度なジェスチャー

| ジェスチャー | 動作 | ショートカット | 用途 |
| ------------ | ---- | -------------- | ---- |
| 2本指スワイプ右 | ブラウザ「戻る」 | Alt + ← | ページ履歴を戻る |
| 2本指スワイプ左 | ブラウザ「進む」 | Alt + → | ページ履歴を進む |
| 3本指ピンチイン | デスクトップ表示 | Win + D | 全ウィンドウを最小化 |
| 3本指スワイプ | アプリ切り替え | Alt + Tab | 開いているウィンドウ切り替え |
| 4本指スワイプ | タスクビュー | Win + Tab | 仮想デスクトップ一覧 |

<details>
<summary>💡 <b>ジェスチャーのコツ</b></summary>

- **スクロール**: 2本の指を画面に置いたまま、滑らかに動かす
- **ピンチ**: 2本の指を同時に近づける/離す（タップではなく、押したまま）
- **スワイプ**: 素早く一方向にスライド（ゆっくりだとスクロールになります）
- **感度調整**: メニューから「設定」で自分好みに調整可能

</details>

---

### ⌨️ キーボードの使い方

1. **メニューを開く**: トラックパッド画面で1本指長押し
2. **キーボードを選択**: メニューから「キーボード」をタップ
3. **文字入力**: 画面上のキーボードで文字を入力

#### ショートカットキーの使い方

```
例: Ctrl + C (コピー) を送信する場合

1. [Ctrl] キーをタップ（押したまま状態になる）
2. [C] キーをタップ
3. 自動的にCtrlキーが解除される

複数の修飾キーも可能:
Ctrl + Shift + T (閉じたタブを再度開く)
→ [Ctrl] → [Shift] → [T] の順にタップ
```

#### よく使うショートカット例

<details>
<summary>📋 <b>便利なショートカット一覧</b></summary>

| ショートカット | 動作 |
| -------------- | ---- |
| Ctrl + C | コピー |
| Ctrl + V | 貼り付け |
| Ctrl + X | 切り取り |
| Ctrl + Z | 元に戻す |
| Ctrl + Y | やり直し |
| Ctrl + A | すべて選択 |
| Ctrl + F | 検索 |
| Ctrl + S | 保存 |
| Ctrl + W | タブを閉じる |
| Ctrl + T | 新しいタブ |
| Ctrl + Shift + T | 閉じたタブを復元 |
| Alt + F4 | アプリを終了 |
| Win + L | PCをロック |
| Win + E | エクスプローラーを開く |

</details>

---

### 🎯 マクロの使い方

マクロ機能を使えば、複雑なキー操作を1タップで実行できます！

#### Android側（マクロの実行）

1. メニューから「マクロ」を選択
2. 登録済みのマクロ一覧が表示されます
3. 実行したいマクロをタップ → 即座に実行！

#### Windows側（マクロの登録・編集）

<details>
<summary><b>新規マクロの作成手順</b></summary>

1. メインウィンドウの「マクロ管理」ボタンをクリック
2. 「新規マクロ」をクリック
3. **マクロ名**を入力（例: "VSCode起動"）
4. **キーを追加**ボタンで順番にキーを追加
   ```
   例: Ctrl + Shift + P (VSCodeコマンドパレット)
   → Ctrl を追加
   → Shift を追加
   → P を追加
   ```
5. 「保存」をクリック
6. Android側に自動的に同期されます ✨

</details>

<details>
<summary><b>マクロの編集・削除</b></summary>

- **編集**: マクロ一覧から編集したいマクロを選択 → 「編集」
- **削除**: マクロを選択 → 「削除」 → 確認ダイアログで「はい」

</details>

#### マクロの活用例

```javascript
// 例1: よく使うアプリを起動
マクロ名: "Chrome起動"
キー: Win, "chrome", Enter

// 例2: スクリーンショット
マクロ名: "スクショ"
キー: Win, Shift, S

// 例3: 音量調整
マクロ名: "ミュート"
キー: VolumeDown (複数回登録可能)

// 例4: 開発環境セットアップ
マクロ名: "開発開始"
キー: Win, "code", Enter, Ctrl, ` (ターミナル起動)
```

> [!TIP]
> マクロは `macros.json` に保存されるため、バックアップも簡単です！

---

### ⚙️ 設定のカスタマイズ

トラックパッド画面のメニューから「設定」を選択すると、以下をカスタマイズできます：

| 設定項目 | 説明 | デフォルト値 |
| -------- | ---- | ------------ |
| マウス感度 | カーソルの移動速度 | 1.5 |
| スクロール感度 | スクロールの速度 | 0.5 |
| ナチュラルスクロール | macOS風のスクロール方向 | OFF |
| タップでクリック | タップを左クリックとして認識 | ON |
| 触覚フィードバック | バイブレーションの有無 | ON |

<p align="right">(<a href="#top">トップへ</a>)</p>

## 開発環境構築

### 必要なツール

#### Windows開発

- Visual Studio 2022 または Visual Studio Code
- .NET 8.0 SDK
- Windows 10/11

#### Android開発

- Android Studio Hedgehog (2023.1.1) 以降
- JDK 17
- Android SDK (API Level 34)

### ビルド手順

#### Windows

```powershell
# 依存関係の復元
cd windows/GlideDeckReceiver
dotnet restore

# デバッグビルド
dotnet build

# リリースビルド
dotnet build -c Release

# 単一ファイル実行可能ファイルの作成
dotnet publish -c Release -r win-x64 --self-contained true -p:PublishSingleFile=true -p:IncludeNativeLibrariesForSelfExtract=true
```

#### Android

```bash
# Gradleビルド
cd android
./gradlew assembleDebug

# リリースビルド（署名が必要）
./gradlew assembleRelease
```

### デバッグ方法

#### Windows

Visual Studio または Visual Studio Code でプロジェクトを開き、F5キーでデバッグ実行

#### Android

Android Studio でプロジェクトを開き、実機またはエミュレータを選択して実行

<p align="right">(<a href="#top">トップへ</a>)</p>

## トラブルシューティング

> [!NOTE]
> 問題が解決しない場合は、[Issues](https://github.com/mmlab-project/Touch-track-pad/issues) で報告してください。

---

### 🔌 接続できない

<details>
<summary><b>❌ QRコードが読み取れない</b></summary>

**症状**: カメラは起動するが、QRコードを認識しない

**解決方法**:
1. ✅ カメラ権限が許可されているか確認
   - 設定 → アプリ → GlideDeck → 権限 → カメラ
2. ✅ QRコードが画面全体に表示されるように距離を調整
3. ✅ 照明が暗すぎる場合は明るくする
4. ✅ カメラレンズが汚れていないか確認
5. ✅ Windows側のアプリを再起動してQRコードを再生成

</details>

<details>
<summary><b>❌ 接続が確立されない</b></summary>

**症状**: QRコードは読み取れるが、接続できない

**解決方法**:

**1. ネットワーク確認**
```powershell
# Windows側でIPアドレスを確認
ipconfig

# Android側で同じネットワークに接続されているか確認
設定 → Wi-Fi → 接続中のネットワーク名を確認
```

- ✅ PCとスマホが**同じWi-Fiネットワーク**に接続されているか
- ✅ ゲストネットワークではなく、メインネットワークに接続しているか
- ✅ VPN接続を無効にする（VPNが原因で接続できない場合があります）

**2. ファイアウォール確認**
```
設定 → プライバシーとセキュリティ → Windowsセキュリティ 
→ ファイアウォールとネットワーク保護
→ 「アプリケーションをファイアウォール経由で許可する」
```
- ✅ `GlideDeckReceiver.exe` がプライベートネットワークで許可されているか
- ✅ 許可されていない場合は「設定の変更」→「別のアプリの許可」から追加

**3. ルーター設定確認**
- ✅ ルーターの**AP分離機能**（クライアント分離）が無効になっているか
  - AP分離が有効だと、同じWi-Fi内でもデバイス間通信ができません
  - ルーターの管理画面で確認・無効化してください

**4. ポート確認**
```powershell
# ポート12345が使用されているか確認
netstat -ano | findstr :12345
```

</details>

<details>
<summary><b>❌ 接続が頻繁に切れる</b></summary>

**症状**: 接続できるが、すぐに切断される

**解決方法**:
- ✅ Wi-Fiの電波強度を確認（PCとスマホをルーターに近づける）
- ✅ 2.4GHz帯ではなく**5GHz帯**のWi-Fiを使用する（推奨）
- ✅ 他のデバイスが帯域を占有していないか確認
- ✅ スマホの省電力モードを無効にする
- ✅ スマホのWi-Fi最適化機能を無効にする
  - 設定 → Wi-Fi → 詳細設定 → Wi-Fi最適化 → OFF

</details>

---

### 💻 Windows側のエラー

<details>
<summary><b>❌ アプリケーションが起動しない</b></summary>

**エラーメッセージ**:
```
.NET Runtime が見つかりません
または
This application requires the .NET Runtime
```

**解決方法**:
1. [.NET 8.0 Runtime](https://dotnet.microsoft.com/download/dotnet/8.0) をダウンロード
2. **Windows x64** 版のインストーラーを実行
3. インストール後、PCを再起動
4. `GlideDeckReceiver.exe` を再度実行

</details>

<details>
<summary><b>❌ ポートが使用中</b></summary>

**エラーメッセージ**:
```
ポート 12345 は既に使用されています
または
Address already in use
```

**解決方法**:

**方法1: 使用中のプロセスを終了**
```powershell
# 1. ポートを使用しているプロセスを確認
netstat -ano | findstr :12345

# 2. プロセスIDを確認（最後の数字）
# 例: TCP    0.0.0.0:12345    0.0.0.0:0    LISTENING    12345
#                                                        ↑このID

# 3. プロセスを終了
taskkill /PID <プロセスID> /F
```

**方法2: 別のポートを使用**
- Windows側のアプリで設定からポート番号を変更
- 変更後、QRコードを再スキャン

</details>

<details>
<summary><b>❌ マウス操作が効かない</b></summary>

**症状**: 接続はできるが、カーソルが動かない

**解決方法**:
- ✅ 管理者権限で実行してみる
  - `GlideDeckReceiver.exe` を右クリック → 「管理者として実行」
- ✅ セキュリティソフトがブロックしていないか確認
- ✅ Windows Updateを実行して最新の状態にする

</details>

---

### 📱 Android側のエラー

<details>
<summary><b>❌ アプリがクラッシュする</b></summary>

**症状**: アプリを起動すると強制終了される

**解決方法**:
1. ✅ Android OSのバージョンを確認
   - 設定 → デバイス情報 → Androidバージョン
   - **Android 8.0 (API 26) 以上**が必要です
2. ✅ アプリを再インストール
   ```
   1. アプリをアンインストール
   2. 端末を再起動
   3. 最新版のAPKを再インストール
   ```
3. ✅ アプリのキャッシュをクリア
   - 設定 → アプリ → GlideDeck → ストレージ → キャッシュを削除

</details>

<details>
<summary><b>❌ カメラが起動しない</b></summary>

**症状**: QRスキャナー画面が真っ黒

**解決方法**:
1. ✅ カメラ権限を確認
   ```
   設定 → アプリ → GlideDeck → 権限 → カメラ → 許可
   ```
2. ✅ 他のアプリがカメラを使用していないか確認
   - カメラアプリなどを終了してから再試行
3. ✅ 端末を再起動
4. ✅ アプリを再インストール

</details>

<details>
<summary><b>❌ 操作が反応しない</b></summary>

**症状**: トラックパッド画面で指を動かしても反応がない

**解決方法**:
1. ✅ Wi-Fi接続が安定しているか確認
   - 画面上部の接続ステータスを確認
2. ✅ Windows側のアプリが起動しているか確認
3. ✅ アプリを再起動
   - アプリを完全に終了（タスクキルではなく、設定から強制停止）
   - 再度起動して接続
4. ✅ スマホの画面タッチが正常に動作するか確認
   - 他のアプリでタッチ操作を試す

</details>

---

### ⚡ パフォーマンスの問題

<details>
<summary><b>🐌 遅延が大きい</b></summary>

**症状**: カーソルの動きが遅れる、操作がカクカクする

**解決方法**:

**ネットワーク最適化**:
- ✅ **5GHz帯のWi-Fi**を使用する（2.4GHz帯より高速）
  ```
  設定 → Wi-Fi → ネットワーク名を確認
  (5GHz帯は通常 "SSID-5G" のような名前)
  ```
- ✅ PCとスマホをルーターに近づける
- ✅ 他のデバイスの帯域使用を減らす
  - 動画ストリーミング、大容量ダウンロードを一時停止

**デバイス最適化**:
- ✅ スマホのバックグラウンドアプリを終了
- ✅ PCのCPU使用率を確認
  ```
  タスクマネージャー (Ctrl + Shift + Esc) で確認
  CPU使用率が90%以上の場合は他のアプリを終了
  ```
- ✅ Windows Defenderのフルスキャンが実行中でないか確認

</details>

<details>
<summary><b>🖱️ カーソルの動きがカクカクする</b></summary>

**症状**: カーソルが滑らかに動かない

**解決方法**:
1. ✅ トラックパッド画面の設定で**マウス感度を調整**
   - メニュー → 設定 → マウス感度 → 0.5〜2.0の範囲で調整
2. ✅ PCのマウス設定を確認
   ```
   設定 → Bluetoothとデバイス → マウス
   → 「マウスポインターの速度」を調整
   ```
3. ✅ PCのグラフィックドライバを最新に更新
4. ✅ Windows側のアプリを管理者権限で実行

</details>

<details>
<summary><b>🔋 スマホのバッテリー消費が激しい</b></summary>

**症状**: アプリ使用中にバッテリーが急速に減る

**解決方法**:
- ✅ 画面の明るさを下げる
- ✅ 触覚フィードバックを無効にする
  - メニュー → 設定 → 触覚フィードバック → OFF
- ✅ 使用しない時はアプリを終了する
- ✅ スマホを充電しながら使用する

</details>

<p align="right">(<a href="#top">トップへ</a>)</p>

## 🤝 コントリビューション

プルリクエストを歓迎します！バグ報告や機能要求も大歓迎です。

### コントリビューションの流れ

1. このリポジトリをフォーク
2. 機能ブランチを作成
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. 変更をコミット
   ```bash
   git commit -m 'Add some amazing feature'
   ```
4. ブランチにプッシュ
   ```bash
   git push origin feature/amazing-feature
   ```
5. プルリクエストを作成

### コントリビューションガイドライン

- **コードスタイル**: 既存のコードスタイルに従ってください
- **コミットメッセージ**: 明確で簡潔なメッセージを心がけてください
- **テスト**: 新機能には適切なテストを追加してください
- **ドキュメント**: READMEやコメントも忘れずに更新してください

> [!IMPORTANT]
> 大きな変更を行う場合は、事前に [Issue](https://github.com/mmlab-project/Touch-track-pad/issues) で相談してください。

<p align="right">(<a href="#top">トップへ</a>)</p>

## 📄 ライセンス

このプロジェクトのライセンスについては、今後決定予定です。

<p align="right">(<a href="#top">トップへ</a>)</p>

## 👤 作者

**mmlab-project**

- 🌐 GitHub: [@mmlab-project](https://github.com/mmlab-project)
- 📧 Email: [お問い合わせはIssuesまで](https://github.com/mmlab-project/Touch-track-pad/issues)

<p align="right">(<a href="#top">トップへ</a>)</p>

## 🙏 謝辞

このプロジェクトは以下の素晴らしい技術・ライブラリを使用しています：

### Android側
- **[Jetpack Compose](https://developer.android.com/jetpack/compose)** - モダンなUIツールキット
- **[CameraX](https://developer.android.com/training/camerax)** - カメラ機能の実装
- **[ML Kit](https://developers.google.com/ml-kit)** - QRコード認識
- **[Material Design 3](https://m3.material.io/)** - デザインシステム
- **[Kotlin](https://kotlinlang.org/)** - プログラミング言語

### Windows側
- **[.NET 8.0](https://dotnet.microsoft.com/)** - アプリケーションフレームワーク
- **[WPF](https://github.com/dotnet/wpf)** - デスクトップUIフレームワーク
- **[QRCoder](https://github.com/codebude/QRCoder)** - QRコード生成
- **[Newtonsoft.Json](https://www.newtonsoft.com/json)** - JSON処理

### インスピレーション
- **Apple Magic Trackpad** - ジェスチャー操作のインスピレーション
- オープンソースコミュニティの皆様

---

<div align="center">

**⭐ このプロジェクトが役に立ったら、スターをつけていただけると嬉しいです！ ⭐**

Made with ❤️ by mmlab-project

</div>

<p align="right">(<a href="#top">トップへ</a>)</p>
