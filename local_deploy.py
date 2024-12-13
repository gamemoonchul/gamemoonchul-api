import asyncio
import subprocess

async def docker_pull(image):
    process = await asyncio.create_subprocess_shell(
        f'docker pull {image}',
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE
    )
    stdout, stderr = await process.communicate()
    if process.returncode == 0:
        print(f'Successfully pulled {image}')
    else:
        print(f'Failed to pull {image}: {stderr.decode()}')

async def main():
    images = [
        'openjdk:17',
        'mysql:8.4.3',
        'redis:latest',
        'bitnami/grafana:latest',
        'prom/prometheus:latest'
    ]
    tasks = [docker_pull(image) for image in images]
    await asyncio.gather(*tasks)

    # ./gradlew clean build -x test --warning-mode all 명령어 실행
    build_process = await asyncio.create_subprocess_shell(
        './gradlew clean build -x test --warning-mode all',
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE
    )
    stdout, stderr = await build_process.communicate()
    if build_process.returncode == 0:
        print('Build successful')
    else:
        print(f'Build failed: {stderr.decode()}')
        return

    # docker compose up -d --build 명령어 실행
    compose_process = await asyncio.create_subprocess_shell(
        'cd ./docker && sudo docker compose up -d --build && pwd',
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE
    )
    stdout, stderr = await compose_process.communicate()
    if compose_process.returncode == 0:
        print('Docker compose up successful')
    else:
        print(f'Docker compose up failed: {stderr.decode()}')

if __name__ == '__main__':
    asyncio.run(main())
