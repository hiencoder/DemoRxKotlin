package=com.tabio.tabioapp.dev
db=$package
db_path=/data/data/$package/databases
adb shell "run-as $package chmod 777 $db_path"
adb shell "run-as $package chmod 777 $db_path/$db.db"
stamp=`date +%s`
echo 'output db:db/'$stamp'.db'
adb pull $db_path/$db.db db/$stamp.db