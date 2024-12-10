# /bin/zsh
pwd
./gradlew clean build -x test --warning-mode all
sudo docker compose up -d
