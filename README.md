# SmartMouse

SmartMouseは、AndroidスマートフォンをWindows PCのトラックパッド、キーボード、およびマクロコントローラーとして使用するためのアプリケーションです。

## 機能

- **高精度トラックパッド**: スマホ画面を使ったスムーズなカーソル操作。
- **キーボード入力**: アプリ内蔵のキーボードを使用して、PCへ文字入力やショートカットキー送信が可能。
- **マクロ機能**: よく使うキー操作をマクロとして登録し、ワンタップで実行。
- **QRコード接続**: 複雑な設定不要、画面のQRコードを読み取るだけでWi-Fi接続。
- **レスポンシブUI**: ウィンドウサイズに合わせて操作しやすい画面レイアウト。

## 動作環境

- **Windows側**: .NET 8.0 Runtime
- **Android側**: Android 8.0 (API 26) 以上
- **ネットワーク**: PCとスマホが同じWi-Fiネットワークに接続されていること

## インストールと使い方

### Windows (Receiver)

1. [Releasesページ](https://github.com/mmlab-project/Touch-track-pad/releases) から `SmartMouseReceiver.exe` をダウンロードします。
   - **インストール不要**: ダウンロードしたファイルを実行するだけで、すぐに使用可能です（必要なライブラリが含まれています）。
2. アプリケーションを起動します。
    - 初回起動時、ファイアウォールの許可を求められた場合は「許可」してください。
3. 画面に表示されるQRコードを確認します。

### Android (Sender)

1. アプリをインストールして起動します。
2. カメラ権限を許可し、Windows側のQRコードをスキャンします。
3. 接続が完了すると、トラックパッド画面が表示されます。

## 開発者向け情報

### ビルド方法 (Windows)

```powershell
cd windows/SmartMouseReceiver
dotnet build
```

### ビルド方法 (Android)

Android Studioを使用して `android` ディレクトリのプロジェクトを開き、ビルドしてください。

## ライセンス

[License details here if applicable, otherwise remove]
