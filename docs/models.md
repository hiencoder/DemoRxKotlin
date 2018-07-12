Models


Me
---

Title|Parameter|Class|isSave|Description
---|---|---|---|---
UUID|uuid|String|True|多分使わない
tabioId|tabioId|String|True|自動発行されるID
PINコード|pinCode|String|True|自動発行されるパス
トークン|token|String|True|認証トークン
リフレッシュトークン|refreshToken|String|True|リフレッシュ用のトークン
トークンの有効期限|tokenExpires|String|True|トークンの有効期限
会員バーコード画像|barcodeUrl|String|True|バーコード画像のURL
アプリURL|appUrl|String|True|トラッキング用のURL？
表示言語|language|Integer|True|言語（日本語:1, 英語:2）
初期インセンティブの付与有無|givenIncentive|bool|True|インセンティブ付与されたかどうか
ユーザID|userId|String|True|ユーザID
ピース数|piece|Integer|True|ピース数
ピース有効期限|pieceExpires|String|True|ピースの有効期限
ポイント数|point|Integer|True|ポイント数
ポイント有効期限|pointExpires|String|True|ポイントの有効期限
ランク|rank|Integer|True|現在のランク
お知らせ通知フラグ|receiveNews|bool|True|お知らせを受け取るかどうか（0:通知しない,1:通知する）
メルマガフラグ|receiveMailMagazine|bool|True|メルマガを受け取るかどうか（0:通知しない,1:通知する）
起動カウント|appStartCount|Integer|True|アプリの起動回数 ※ID＆PINのモーダルを表示するため等に使用する
会員ステータス|status|Integer|True|会員ステータス（0:通常,1:停止,2:退会）
デバイスID|deviceId|String|True|プッシュ用のID
緯度|latitude|Double|True|緯度
経度|longitude|Double|True|経度
プロフィール|profile|Profile|True|プロフィールへの関連付け
移行区分|routes|Array(Route)|True|Routeへの関連付け
クーポン|coupons|Array(Coupon)|True|Couponへの関連付け
購入履歴|purchasedHistories|Array(Purchase)|False|Purchaseへの関連付け



Profile
---

Title|Parameter|Class|isSave|Description
---|---|---|---|---
顔アイコン画像|iconImgUrl|String|True|アイコン画像のURL
カバー画像|coverImgUrl|String|True|カバー画像のURL
ニックネーム|nickname|String|True|ニックネーム
生年月日|birthday|String|True|生年月日（YYYY/MM/DD）
性別|gender|Integer|True|性別（女性:1, 男性:2）


Coupon
---


Title|Parameter|Class|isSave|Description
---|---|---|---|---
クーポンID|couponId|String|False|クーポンID
クーポン名|name|String|False|クーポン名
クーポン画像|imgUrl|String|False|クーポン画像URL
クーポンコード|code|String|False|クーポンコード
バーコード画像|barcodeImgUrl|String|False|バーコード画像
開始日|startDate|String|False|開始日
終了日|endDate|String|False|終了日
利用方法|use|Integer|False|1:オンライン限定,2:店舗限定
利用可能店舗|stores|String|False|利用可能店舗
注釈|comment|String|False|注釈


Route
---

Title|Parameter|Class|isSave|Description
---|---|---|---|---
移行区分|from|Integer|True|Email:0, Twitter:1, Facebook:2
プロバイダーID|providerId|String|True|Email or TwitterID or FacebookID
セキュリティキー|securityKey|String|True & False|パスワードやトークン。パスワードの場合は保存しない


Product
---

Title|Parameter|Class|isSave|Description
---|---|---|---|---
JAN|jan|String|False|JANコード
商品ID|productId|String|False|商品ID（品番）
商品名|name|String|False|商品名
商品規格ID|classId|String|False|商品規格ID（SKU）
お気に入りフラグ|isFavorite|bool|False|お気に入りフラグ
お気に入り数|favoriteCount|Integer|False|お気に入り数


Asset
---

Title|Parameter|Class|isSave|Description
---|---|---|---|---
メイン画像URL|mainImgUrl|String|False|メイン画像URL
選択済ラインアップ|selectedLineupIndex|Integer|False|選択されているlineup（デフォルトは0）
ラインアップ|lineups|Array(Lineup)|False|ラインアップの配列


Lineup
---

Title|Parameter|Class|isSave|Description
---|---|---|---|---
商品規格ID|classId|String|False|商品規格ID
カラーコード|code|String|False|カラーコード
カラー名|name|String|False|カラー名
カラーチップ画像URL|chipImgUrl|String|False|カラーチップ画像
カラー画像|imgUrl|String|False|カラー画像
通販用販売価格|price|Integer|False|通販用販売価格
販売区分|status|Integer|False|1:通常,2:再入荷リクエスト可能,3:販売終了
在庫有り店舗|stockHasStores|Array(Store)|False|在庫を持っている店舗一覧
メインフラグ|isMain|bool|False|メインかどうか


Stock
---

Title|Parameter|Class|isSave|Description
---|---|---|---|---
商品規格ID|classId|String|False|商品規格ID
在庫区分|status|Integer|False|0:在庫なし,1:在庫あり,2:残り僅か


Item
---

Title|Parameter|Class|isSave|Description
---|---|---|---|---
品番|itemId|String|False|品番
定価|price|Integer|False|定価
商品説明|description|String|False|商品説明
サイズ表記|size|String|False|サイズ
素材表記|material|String|False|素材
ブランドID|brand|String|False|ブランドID（ブランド名？）
レビュー数|reviewCount|Integer|False|レビュー数
アセット|asset|Asset|False|アセットの関連付け
レビュー|reviews|Array(Review)|False|レビューの関連付け
雑誌掲載履歴|postHistories|Array(Magazine)|False|マガジンの関連付け
販売開始日|fromDate|String|False|販売開始日


Coordinate
---

Title|Parameter|Class|isSave|Description
---|---|---|---|---
コーディネートID|coordinateId|String|False|コーディネートID
実店舗ID|storeId|String|False|店舗ID
店舗名|storeName|String|False|店舗名
画像URL|imgUrl|String|False|画像URL
カラー画像URL|chipImgUrl|String|False|カラーチップ画像URL



Review
---

Title|Parameter|Class|isSave|Description
---|---|---|---|---
レビューID|reviewId|String|False|レビューID
ニックネーム|nickname|String|False|ニックネーム
顔アイコン画像URL|iconImgUrl|String|False|アイコン画像URL
日付|date|String|False|日付 YYYY/MM/DD hh:mm:ss
本文|comment|String|False|本文


Magazine
---

Title|Parameter|Class|isSave|Description
---|---|---|---|---
雑誌ID|magazineId|String|False|雑誌ID
雑誌名|name|String|False|雑誌名
号|issue|String|False|号
発売日|pubDate|String|False|発売日
掲載ページ|page|Integer|False|掲載ページ


Store
---

Title|Parameter|Class|isSave|Description
---|---|---|---|---
実店舗ID|storeId|String|True|実店舗ID
店舗コード|code|String|True|店舗コード
店舗名|name|String|True|店舗名
緯度|latitude|Double|Ture|緯度
経度|longitude|Double|True|経度
お気に入りフラグ|isFavorite|bool|True|お気に入りフラグ
お気に入り数|favoriteCount|Integer|False|お気に入り数
郵便番号|zip|String|False|
都道府県|prefecture|String|False|
住所|address|String|False|
電話番号|tel|String|False|
アクセス方法|access|String|False|
営業日|operationDate|String|True|
開店時間(平日)|openTimeOfDay|String|True|
閉店時間(平日)|closeTimeOfDay|String|True|
開店時間(祝祭日)|openTimeOfHoliday|String|True|
閉店時間(祝祭日)|closeTimeOfHoliday|String|True|
店舗ブランド|brand|String|False|店舗ブランド
店舗サービス|services|Array[Integer]|False|11:刺繍,12:プリント,13:滑り止め・ツボ押し,21:会員証アプリ,22:銀聯カード,23:免税対応
店舗商品|item_services|Array[Integer]|False|1:メンズ,2:レディース,3:キッズ
チェックイン|checkins|Array(Checkin)|True|チェックインへの関連
在庫情報|stocks|Array(Stock)|False|在庫情報


Checkin
---

Title|Parameter|Class|isSave|Description
---|---|---|---|---
実店舗ID|storeId|String|True|実店舗ID
店舗コード|storeCode|String|True|店舗コード
チェックイン日時|timestamp|String|True|チェックインした日時


Purchase
---

Title|Parameter|Class|isSave|Description
---|---|---|---|---
購入ID|purchaseId|String|False|購入ID
購入日|purchasedDate|String|False|購入日
店舗名|storeName|String|False|店舗名
ステータス|status|Integer|False|1:購入済,2:注文完了,3:発注待ち,4:発注済,5:キャンセル,6:返品
購入金額|price|Integer|False|購入金額
消費税|tax|Integer|False|消費税
送料|deliverFee|Integer|False|送料
ギフトラッピングの金額|giftPrice|Integer|False|ギフトラッピングの金額
ポイント値引き金額|pointDiscountPrice|Integer|False|ポイント値引き金額
クーポン値引き金額|couponDiscountPrice|Integer|False|クーポン値引き金額
今回付与ピース数|addPiece|Integer|False|今回付与ピース
詳細|details|Array(PurchaseDetail)|False|詳細一覧

PurchaseDetail
---

Title|Parameter|Class|isSave|Description
---|---|---|---|---
購入明細ID|purchaseDetailId|String|False|購入明細ID
商品|item|Item|False|商品
単価|price|Integer|False|単価
数量|quantity|Integer|False|数量


CoordinateFilter
---

Title|Parameter|Class|isSave|Description
---|---|---|---|---
検索タイプ|searchType|Integer|False|検索区分(0:全部, 1:お気に入り)
JANコード|jan|String|False|JANコード
商品ID|productId|String|False|商品ID
商品規格ID|classId|String|False|商品規格ID
店舗コード|storeCode|String|False|店舗コード
検索結果件数|total|Integer|False|検索結果数
検索結果|coordinates|Array(Coordinate)|False|コーディネートの検索結果
ページ数|page|Integer|False|ページ数
リミット|limit|Integer|False|リミット
表示言語|language|String|False|表示言語


StoreFilter
---

Title|Parameter|Class|isSave|Description
---|---|---|---|---
検索タイプ|searchType|Integer|False|検索区分(0:全部, 1:お気に入り)
キーワード|keyword|String|False|キーワード
都道府県|prefectures|String|False|都道府県（ID）
店舗ブランド|brand|String|False|店舗ブランド
店舗サービス|services|Array(String)|店舗サービス
店舗商品|item_services|Array(String)|店舗商品
ページ数|page|Integer|False|ページ数
リミット|limit|Integer|False|リミット
検索結果|result|Array(Store)|False|検索結果
表示言語|language|String|False|表示言語


ItemFilter
---

Title|Parameter|Class|isSave|Description
---|---|---|---|---
検索タイプ|searchType|Integer|False|検索区分(1:全部,2:お気に入り,3:閲覧履歴)
キーワード|keyword|String|False|キーワード
性別タイプ|gender|Array(Integer)|False|性別タイプ(1:メンズ,2:レディース,3:キッズ)
種類|type|Array(Integer)|False|種類（1:ソックス,2:カバーソックス,3:サンダルソックス,4:タイツ,5:ストッキング,6:レギンス,7:トレンカ,8:レッグウォーマー）
丈|length|Array(Integer)|False|丈（1:カバーソックス,2:スニーカーソックス,3:ショート・アンクルソックス,4:クルー(ふくらはぎ下),5:ハイカット・ハイソックス,6:ニーソックス）
足のサイズ（下限）|minSize|Float|False|足のサイズ下限
足のサイズ（上限）|maxSize|Float|False|足のサイズ上限
足指区分|toeType|Array(Integer)|False|足指区分（1:五本指,2:タビ）
素材|material|Array(Integer)|False|1:綿100%,2:シルク,3:麻,4:毛,5:キュプラ,6:カシミヤ,7:アンゴラ,8:オーガニックコットン
機能|function|Array(Integer)|False|1:冷えとり,2:むくみ（着圧靴下）,3:ムレ・臭い・水虫,4:乾燥肌・敏感肌,5:足の疲れ,6:足の締め付け,7:滑り止め,8:スポーツ,9:おやすみ(熟睡したい方)
カラー|color|Array(Integer)|False|1:白,2:黒,3:グレー,4:ベージュ,5:茶,6:ピンク,7:赤,8:オレンジ,9:イエロー,10:グリーン,11:ブルー,12:ネイビー,13:パープル
柄|pattern|Array(Integer)|False|1:無地,2:ラメ,3:リブ,4:ボーダー,5:アーガイル・タイル,6:ドット・水玉,7:星柄,8:花柄,9:ストライプ,10:チェック,11:リボン,12:ハート柄,13:ジャガード,14:メッシュ・ラッセル・綱
ギフト|gift|Array(Integer)|False|1:なし,2:ギフトラッピング,3:ギフトセット
雑誌|magazine|Magazine|False|雑誌
ブランド|brand|Array(Integer)|False|???
価格下限|minPrice|Integer|False|価格下限
価格上限|maxPrice|Integer|False|価格上限
表示順|order|Integer|False|1:新着順,2:売り上げ・人気順,3:価格が安い順,4:価格が高い順
関連商品情報|relate|boolean|False|1:関連商品情報を返却する
検索結果|result|Array(Item)|False|検索結果
ページ数|page|Integer|False|ページ数
リミット|limit|Integer|False|リミット
表示言語|language|String|False|表示言語