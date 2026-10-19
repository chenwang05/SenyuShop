# ============================================================
#  森屿商店 SenyuShop Windows 编译脚本
#  1) 把依赖 jar 放进 .\build-tools\(paper-api.jar、VaultSlim.jar、runtime\*.jar)
#  2) Ensure JDK(含 javac / jar)路径正确
#  3) 直接运行 .\build.ps1,产物在 .\build\dist\
# ============================================================
$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot

# ==== 按需修改:JDK 与插件版本 ====
$jdk   = 'C:\Program Files\Java\jdk-25.0.4\bin'
$ver   = '1.1.2'
$tools = Join-Path $root 'build-tools'
$rt    = Join-Path $tools 'runtime'

$cp = @(
  "$tools\paper-api.jar",
  "$tools\VaultSlim.jar",
  "$rt\adventure-api-5.2.0.jar",
  "$rt\adventure-key-5.2.0.jar",
  "$rt\adventure-text-serializer-legacy-5.2.0.jar",
  "$rt\adventure-text-serializer-plain-5.2.0.jar",
  "$rt\option-1.1.0.jar",
  "$rt\examination-api-1.3.0.jar",
  "$rt\annotations-26.0.2-1.jar",
  "$rt\guava-33.6.0-jre.jar",
  "$rt\failureaccess-1.0.3.jar",
  "$rt\bungeecord-chat-1.21-R0.2-deprecated+build.21.jar"
) -join ';'

# ==== 编译 ====
$src = Join-Path $root 'src'
$out = Join-Path $root 'build\out'
if (Test-Path $out) { Remove-Item $out -Recurse -Force }
New-Item -ItemType Directory -Force -Path $out | Out-Null

$files = Get-ChildItem $src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
Write-Output "compiling $($files.Count) files"
& "$jdk\javac.exe" -encoding UTF-8 -proc:none -cp $cp -d $out $files
if ($LASTEXITCODE -ne 0) { throw 'javac failed' }
Write-Output 'JAVAC-OK'

# ==== 打包 ====
$stage = Join-Path $root 'build\stage'
$dist  = Join-Path $root 'build\dist'
if (Test-Path $stage) { Remove-Item $stage -Recurse -Force }
New-Item -ItemType Directory -Force -Path $stage, $dist | Out-Null
Copy-Item "$out\senyu" "$stage\" -Recurse
Copy-Item (Join-Path $root 'resources\plugin.yml'), (Join-Path $root 'resources\config.yml') $stage

Push-Location $stage
& "$jdk\jar.exe" cf "$dist\SenyuShop-$ver.jar" .
Pop-Location
Get-Item "$dist\SenyuShop-$ver.jar" | Select-Object Name, Length
