@echo Offline Data for App

@echo Written by Arman Hossain
@echo Date: 16/04/2020

@echo This file downloads existing API Data
@echo into assets folder. These files
@echo are exported to release build for installation.

curl -o "app\src\main\assets\data\story.json" https://ureport-offline.unicefbangladesh.org/api/story?limit=1000
curl -o "app\src\main\assets\data\ureport.json" https://ureport-offline.unicefbangladesh.org/api/ureport?limit=1
curl -o "app\src\main\assets\data\surveyor.json" https://ureport-offline.unicefbangladesh.org/api/surveyor?limit=1000
curl -o "app\src\main\assets\data\deleted.json" https://ureport-offline.unicefbangladesh.org/api/log
python download_files.py