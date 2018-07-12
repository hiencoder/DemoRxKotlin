screen_key=$3

title="GCM Test"
message="「"
case "$screen_key" in
	"1" ) message+="商品詳細";;
	"2" ) message+="WebView("$4")";;
	"3" ) message+="店舗詳細";;
	"4" ) message+="マイページ";;
	"5" ) message+="アカウントプレビュー";;
	"6" ) message+="その他";;
	"7" ) message+="FAQ";;
	"8" ) message+="チェックイン";;
	"9" ) message+="会員証";;
	"10" ) message+="コーディネート";;
	"11" ) message+="商品一覧";;
	"12" ) message+="店舗一覧";;
	"13" ) message+="クーポン一覧";;
	"14" ) message+="メールアドレス・パスワード登録";;
	"15" ) message+="メールアドレス・パスワードログイン(会員移行)";;
	"16" ) message+="オンライン会員ログイン(会員移行)";;
	"17" ) message+="通知設定";;
	"18" ) message+="言語設定";;
	"19" ) message+="カート";;
	"20" ) message+="チュートリアル";;
	"21" ) message+="レビュー投稿";;
	"22" ) message+="ID・PIN確認";;
esac

message+="」画面を開きます"

curl --header "Authorization: key=$1" \
--header Content-Type:"application/json" \
https://android.googleapis.com/gcm/send \
-d "{\"registration_ids\":[\"$2\"],\
\"data\":{\"title\":\"$title\",\"message\":\"$message\",\"screen_key\":\"$screen_key\", \"identifier\":\"$4\"}}"
