[![Build Status](https://travis-ci.org/mozq/picto.svg?branch=master)](https://travis-ci.org/mozq/picto)
[ ![Download](https://api.bintray.com/packages/mozq/generic/picto/images/download.svg) ](https://bintray.com/mozq/generic/picto/_latestVersion)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

# Picto

Pictoは写真を整理するためのデスクトップ・アプリです。

主に、下記のことが可能です。

- 写真のコピー・移動
    - 写真の撮影日などを利用した自由なファイル名やフォルダー名で、写真をコピー・移動できます。
- 写真ファイルの日付の変更
    - 写真ファイルの作成日、更新日、アクセス日、Exif撮影日を変更できます。海外旅行などでカメラのタイムゾーンを変更し忘れた際に有効です。
- Exif情報の削除
    - GPS情報の削除や、Exif情報の全削除ができます。


## インストール

### Macへのインストール
1. DMGファイル 'Picto-X.X.X-mac.dmg' を [こちらからダウンロード](https://bintray.com/mozq/generic/picto/_latestVersion) します。(X.X.X はバージョンです。)
2. DMGをマウントして Picto.app をアプリケーション・フォルダーにコピーします。
3. Picto.app を実行して起動します。
    - もし「開発元が未確認のため開けません」という警告ダイアログが表示された場合は、下記の手順をお試しください。
        1. アプリケーションを右クリック (または control＋クリック) して「開く」をクリックします。
        2. 次に表示される警告ダイアログで「開く」ボタンをクリックしてアプリケーションを起動します。

### Windowsへのインストール
1. [Java Runtime Environment](https://java.com/ja/download/) がインストールされていない場合はインストールします。
2. Zipファイル 'Picto-X.X.X-win.zip' を [こちらからダウンロード](https://bintray.com/mozq/generic/picto/_latestVersion) します。(X.X.X はバージョンです。)
3. Zipファイルを展開し、 Picto.exe を好きなフォルダーにコピーします。
4. Picto.exe を実行して起動します。

### その他OSへのインストール
1. [Java Runtime Environment](https://java.com/ja/download/) がインストールされていない場合はインストールします。
2. JARファイル 'Picto-X.X.X-all.jar' を [こちらからダウンロード](https://bintray.com/mozq/generic/picto/_latestVersion) します。(X.X.X はバージョンです。)
3. JARファイルを好きなフォルダーにコピーします。
4. JARファイルを `java -jar Picto-X.X.X-all.jar` のように起動します。


## アンインストール
コピーしたアプリケーションファイルを削除することで、アンインストールできます。


## 設定

### 対象

#### 写真フォルダー
写真や動画ファイルがあるフォルダーを選択します。

#### ファイル名パターン
対象のファイルをファイル名のパターンで指定できます。
[Glob](https://en.wikipedia.org/wiki/Glob_%28programming%29) または [正規表現](https://ja.wikipedia.org/wiki/%E6%AD%A3%E8%A6%8F%E8%A1%A8%E7%8F%BE) でパターンを指定できます。

- Glob: "*.jpg", "IMG_????.*"
- 正規表現: ".*\\.jpg", "IMG_[0-9]{4}\\..*"

#### 隠しファイルとフォルダーを含む
このオプションをチェックすると、隠しファイルとフォルダーを対象に含めます。

#### サブフォルダーのファイルを含む
このオプションをチェックすると、サブファイルとフォルダーを対象に含めます。

#### ファイルサイズ
ファイルサイズで対象をフィルタリングします。

#### 作成日時
作成日時で対象をフィルタリングします。

#### 更新日時
更新日時で対象をフィルタリングします。


### 宛先

#### 操作
- コピー -- ファイルを写真フォルダーから宛先フォルダーにコピーします。
- 移動 -- ファイルを写真フォルダーから宛先フォルダーに移動します。
- 上書き -- 写真フォルダーのファイルを上書きします。

#### 宛先フォルダー
コピーまたは移動先のフォルダーを指定します。

#### サブパスのパターン
宛先フォルダー内のファイルのサブパスを指定します。
下記の変数を'${}'付きで指定可能です。

    例)
    "${ParentSubPath}/${PhotoTakenDate%uuuu/MM}/${FileName}"
    -> "subpath/to/2012/01/IMG_0001.JPG"

|変数                      |型       |意味                                   |例                      |
|-------------------------|--------|-----------------------------------------|------------------------|
|Now                      |日時    |現在の日時                               |2012-01-23 12:34:56.780 |
|ParentSubPath            |文字列  |ファイルの親フォルダーのパス             |subpath/to              |
|FileName                 |文字列  |ファイルの名前                           |IMG_0001.JPG            |
|BaseName                 |文字列  |ファイルのベース名                       |IMG_0001                |
|Extension                |文字列  |ファイルの拡張子                         |.JPG                    |
|Size                     |文字列  |ファイルのサイズ                         |10485760                |
|CreationDate             |日時    |ファイルの作成日時                       |2012-01-23 12:34:56.780 |
|ModifiedDate             |日時    |ファイルの更新日時                       |2012-01-23 12:34:56.780 |
|AccessDate               |日時    |ファイルのアクセス日時                   |2012-01-23 12:34:56.780 |
|PhotoTakenDate           |日時    |Exifの撮影日時、またはファイルの更新日時 |2012-01-23 12:34:56.780 |
|Width                    |整数    |Exif 画像の幅                            |6000                    |
|Height                   |整数    |Exif 画像の高さ                          |4000                    |
|FNumber                  |小数    |Exif F値                                 |8.0                     |
|Aperture                 |小数    |Exif 絞り値                              |6.0                     |
|MaxAperture              |小数    |Exif 最大絞り値                          |3.6                     |
|ISO                      |整数    |Exif ISO                                 |100                     |
|FocalLength              |小数    |Exif 焦点距離                            |26.0                    |
|FocalLength35mm          |小数    |Exif 35mm換算での焦点距離                |39.0                    |
|ShutterSpeed             |小数    |Exif シャッタースピード                  |7.0                     |
|ExposureTime             |小数    |Exif 露出時間（秒）                      |0.008                   |
|ExposureMode             |整数    |Exif 露出モード                          |0: 自動<br>1: マニュアル<br>2: オートブラケット |
|ExposureProgram          |整数    |Exif 露出プログラム                      |0: 未定義<br>1: マニュアル<br>2: ノーマル・プログラム<br>3: 絞り優先<br>4: シャッター優先<br>5: 深度優先<br>6: スポーツ<br>7: 人物<br>8: 風景 |
|Brightness               |整数    |Exif 輝度値                              |2.21                    |
|WhiteBalance             |整数    |Exif ホワイトバランス                    |0: 自動<br>1: マニュアル|
|LightSource              |整数    |Exif 光源                                |0: 不明<br>1: 太陽光<br>2: 蛍光灯<br>3: 白熱電球<br>4: フラッシュ<br>9: 晴天<br>10: 曇天<br>11: 日陰<br>12: 昼光色蛍光灯 (D 5700 - 7100K)<br>13: 昼白色蛍光灯 (N 4600 - 5400K)<br>14: クール白色蛍光灯 (W 3900 - 4500K)<br>15: 白色蛍光灯 (WW 3200 - 3700K)<br>17: 標準ライト A<br>18: 標準ライト B<br>19: 標準ライト C<br>20: D55<br>21: D65<br>22: D75<br>23: D50<br>24: ISO ISO スタジオ電球<br>255: その他 |
|Orientation              |整数    |Tiff 方向                                |[0番目の行, 0番目の列]<br>1: 上, 左<br>2: 上, 右<br>3: 下, 右<br>4: 下, 左<br>5: 左, 上<br>6: 右, 上<br>7: 右, 下<br>8: 左, 下 |
|Lens                     |文字列  |Exif レンズ                              |                        |
|LensMake                 |文字列  |Exif レンズのメーカー                    |                        |
|LensModel                |文字列  |Exif レンズのモデル                      |                        |
|LensSerialNumber         |文字列  |Exif レンズのシリアルナンバー            |                        |
|Make                     |文字列  |Tiff メーカー                            |NIKON CORPORATION       |
|Model                    |文字列  |Tiff モデル                              |NIKON D1                |
|Software                 |文字列  |Exif ソフトウェア                        |Capture NX-D 1.2.0 M    |
|ProcessingSoftware       |文字列  |Exif 処理ソフトウェア                    |                        |
|OwnerName                |文字列  |Exif オーナー名                          |                        |
|CameraOwnerName          |文字列  |Exif カメラオーナー名                    |                        |
|GPSLat                   |小数    |Exif GPS緯度（北基準）                   |35.658581               |
|GPSLatDeg                |小数    |Exif GPS経度（度）                       |35.0                    |
|GPSLatMin                |小数    |Exif GPS経度（分）                       |39.0                    |
|GPSLatSec                |小数    |Exif GPS経度（秒）                       |30.89                   |
|GPSLatRef                |文字列  |Exif GPS経度基準                         |N: 北緯<br>S: 南緯      |
|GPSLon                   |小数    |Exif GPS経度（東基準）                   |139.745433              |
|GPSLonDeg                |小数    |Exif GPS経度（度）                       |139.0                   |
|GPSLonMin                |小数    |Exif GPS経度（分）                       |44.0                    |
|GPSLonSec                |小数    |Exif GPS経度（秒）                       |43.558                  |
|GPSLonRef                |文字列  |Exif GPS経度基準                         |E: 東経<br>W: 西経      |
|GPSAlt                   |小数    |Exif GPS高度 (m)                         |18.4                    |
|GPSAltRef                |文字列  |Exif GPS高度基準                         |0: 海抜標準<br>1: 海抜標準 (負の値) |

##### Format variable values
'${FNumber%0.0}' のように '%' 区切りを使うことで、変数値をフォーマットできます。

###### 整数型、小数型の値のフォーマット
|記号  |指定位置           |意味                                   |
|------|-------------------|---------------------------------------|
|0     |数値               |数字                                   |
|#     |数値               |数字、無い場合はゼロを表示             |
|.     |数値               |小数点                                 |
|,     |数値               |桁区切り                               |
|;     |サブパターン区切り |正の数と負の数でのサブパターンの区切り |
|%     |最初または最後     |100を掛けたパーセント表示              |
詳細はこちら: https://docs.oracle.com/javase/jp/8/docs/api/java/text/DecimalFormat.html

    例)
    "${FNumber%0.0}"
    -> "8.0", "16.0"
    
    "${GPSLatDeg%0}°${GPSLatMin%0}'${GPSLatSec%0.0#} ${GPSLatRef}"
    -> "35°39'30.89 N"

###### 日時型(Date)の値のフォーマット
|記号    |意味                             |例                                             |
|------|-----------------------------------|-----------------------------------------------|
|G     |紀元                               |AD; Anno Domini; A                             |
|u     |年                                 |2004; 04                                       |
|y     |紀元における年                     |2004; 04                                       |
|D     |年における日                       |189                                            |
|M/L   |年における月                       |7; 07; Jul; July; J                            |
|d     |月における日                       |10                                             |
|Q/q   |年におけるクオーター               |3; 03; Q3; 3rd quarter                         |
|Y     |暦週の基準年                       |1996; 96                                       |
|w     |年における週                       |27                                             |
|W     |月における週                       |4                                              |
|E     |週における曜日                     |Tue; Tuesday; T                                |
|e/c   |ローカライズされた週における曜日   |2; 02; Tue; Tuesday; T                         |
|F     |月における週                       |3                                              |
|a     |日におけるAM/PM                    |PM                                             |
|h     |AM/PMにおける時計時間 (1-12)       |12                                             |
|K     |AM/PMにおける時間 (0-11)           |0                                              |
|k     |時計時間 (1-24)                    |0                                              |
|H     |日における時間 (0-23)              |0                                              |
|m     |時間における分                     |30                                             |
|s     |分における秒                       |55                                             |
|S     |秒におけるミリ秒                   |978                                            |
|A     |日におけるミリ秒                   |1234                                           |
|n     |秒におけるナノ秒                   |987654321                                      |
|N     |日におけるナノ秒                   |1234000000                                     |
|V     |タイムゾーンID                     |America/Los_Angeles; Z; -08:30                 |
|z     |タイムゾーン名                     |Pacific Standard Time; PST                     |
|O     |ローカライズされたゾーンオフセット |GMT+8; GMT+08:00; UTC-08:00;                   |
|X     |ゾーンオフセット、ゼロの場合は'Z'  |Z; -08; -0830; -08:30; -083015; -08:30:15;     |
|x     |ゾーンオフセット                   |+0000; -08; -0830; -08:30; -083015; -08:30:15; |
|Z     |ゾーンオフセット                   |+0000; -0800; -08:00;                          |
詳細はこちら: https://docs.oracle.com/javase/jp/8/docs/api/java/time/format/DateTimeFormatter.html

    例)
    "${PhotoTakenDate%uuuu/MMdd}"
    -> "2012/0123"

##### 変数値の変換
'${WhiteBalance/0:Auto/1:Manual}' のように '/' と ':' の区切りを使うことで、変数値を変換できます。

    / <比較式> : <戻り値>

複数の '/' 区切りを指定すると、最初に一致した値が返されます。
どの比較式にも一致しない場合、空の値を返します。

    例)
    "${WhiteBalance/0:Auto/1:Manual}"
    -> "Auto" or "Manual" or "" (When not matching)

比較式には、下記の演算子を使用できます。

|演算子  |意味                 |変数型                   |例                                                                       |
|--------|---------------------|-------------------------|-------------------------------------------------------------------------|
|=       |等しい  (デフォルト) |文字列, 整数, 小数, 日時 |/=str:<br>/str:<br>/'str':<br>/123.456:<br>/#2012-01-23#:                |
|=*      |ワイルドカード一致   |文字列                   |/=*'*str': (後方一致)<br>/=*'str*': (前方一致)<br>/=*'*str*': (部分一致) |
|=~      |正規表現一致         |文字列                   |/=~'str[0-9]{4}':                                                        |
|!=      |等しく無い           |文字列, 整数, 小数, 日時 |/!=str:<br>/!='str':<br>/!=123.456:<br>/!=#2012-01-23#:                  |
|<       |より小さい           |整数, 小数, 日時         |/<123.456: <br> /<#2012-01-23#:                                          |
|<=      |以下                 |整数, 小数, 日時         |/<=123.456: <br> /<=#2012-01-23#:                                        |
|>       |より大きい           |整数, 小数, 日時         |/>123.456: <br> />#2012-01-23#:                                          |
|>=      |以上                 |整数, 小数, 日時         |/>=123.456: <br> />=#2012-01-23#:                                        |

比較式に 'default' ラベルを指定した場合、どんな値にもマッチします。

    例)
    "${Make/=*'NIKON*':Nikon/'Canon':Canon/default:Others}"
    -> "Nikon" or "Canon" or "Others"


#### 同名ファイルが存在する場合
同名のファイルが存在する場合の処理方法を指定します。

- 確認 -- 確認ダイアログを表示します。
- 上書き -- ファイルを上書きします。
- スキップ -- そのファイルをスキップして処理を継続します。
- 中断 -- 処理を中断します。

#### ファイルの検証 - ファイルのダイジェスト値を比較
チェックをすると、ファイルの内容が正常にコピーされたかを確認します。
ファイルのExif情報を削除するといったファイルを変更するオプションを選択している場合、この指定は無視されます。


### ファイル日時の変更

#### 変更対象
変更対象の日時を指定します。

- 作成日時 -- ファイルの作成日時を更新します。
- 更新日時 -- ファイルの更新日時を更新します。
- アクセス日時 -- ファイルのアクセス日時を更新します。
- Exif日時 -- ファイルのExif日時を更新します。

#### 変更値
変更対象の日時として設定する日時を指定します。

- 現在日時 -- 現在日時で変更します。
- ファイル作成日時 -- ファイルの作成日時で変更します。
- ファイル更新日時 -- ファイルの更新日時で変更します。
- ファイルアクセス日時 -- ファイルのアクセス日時で変更します。
- ファイルExif日時 -- ファイルのExif日時で変更します。
- 指定日時 -- 入力した日時で変更します。

#### 変更値の更新
更新値の差分を設定します。

- なし
- 加算
- 減算
- 上書き


### Exifの変更
- GPSのExifタグのみを削除する
- 全てのExifタグを削除する


### その他

#### テスト実行
処理をシミュレーションします。実際にコピーやファイルの変更は行われません。


## ライセンス
Pictoはオープンソースソフトウェアで、[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)のもとに公開されています。


## リポジトリー
Pictoは[GitHub](https://github.com/mozq/picto)で開発・管理されています。


## リーガル
Pictoは下記のオープンソース・ソフトウェアを使用しています。

### Apache Commons Imaging (Sanselan)
> Apache Sanselan
> Copyright 2007-2009 The Apache Software Foundation.
> 
> This product includes software developed at
> The Apache Software Foundation (http://www.apache.org/).

### mifmi-commons4j
> The MIT License (MIT)
> 
> Copyright (c) 2015 mifmi.org and other contributors
> 
> Permission is hereby granted, free of charge, to any person obtaining a copy
> of this software and associated documentation files (the "Software"), to deal
> in the Software without restriction, including without limitation the rights
> to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
> copies of the Software, and to permit persons to whom the Software is
> furnished to do so, subject to the following conditions:
> 
> The above copyright notice and this permission notice shall be included in all
> copies or substantial portions of the Software.
> 
> THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
> IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
> FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
> AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
> LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
> OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
> SOFTWARE.
