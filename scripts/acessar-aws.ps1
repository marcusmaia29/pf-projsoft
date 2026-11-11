param([string]$KeyPath = 'C:\Users\Marqu\Downloads\projsoft26b.pem')
$ErrorActionPreference = 'Stop'
if (-not (Test-Path -LiteralPath $KeyPath)) { throw "Chave não encontrada: $KeyPath" }
$sshClient = 'C:\Program Files\Git\usr\bin\ssh.exe'
if (-not (Test-Path -LiteralPath $sshClient)) { $sshClient = (Get-Command ssh).Source }
& $sshClient -o StrictHostKeyChecking=yes -o ConnectTimeout=10 -i $KeyPath ubuntu@3.227.9.20
