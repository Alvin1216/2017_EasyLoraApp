# 2017_EasyLoraApp
一個簡單開關手機手電筒的app(透過lora)
####架構:
*先透過mdot把光源sensor的資料用mqtt的模式上傳到free broker
*app訂閱freebroker的資料夾，收到如果光源急速減少，那就把手機的手電筒打開
