# /bin/zsh
./gradlew clean build -x test --warning-mode all
cd ./docker && sudo docker compose up -d --build && pwd
