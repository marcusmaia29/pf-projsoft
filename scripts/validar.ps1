param([string]$BaseUrl = 'http://3.227.9.20:8080')
$ErrorActionPreference = 'Stop'
$BaseUrl = $BaseUrl.TrimEnd('/')
$client = [System.Net.Http.HttpClient]::new()
$client.Timeout = [TimeSpan]::FromSeconds(15)
try {
    $health = $client.GetAsync("$BaseUrl/actuator/health").GetAwaiter().GetResult()
    $body = $health.Content.ReadAsStringAsync().GetAwaiter().GetResult()
    if ([int]$health.StatusCode -ne 200 -or ($body | ConvertFrom-Json).status -ne 'UP') {
        throw "Saúde da API falhou: HTTP $([int]$health.StatusCode)"
    }
    Write-Host 'OK: aplicação e banco saudáveis.' -ForegroundColor Green
    foreach ($route in @('/base/status', '/base/admin')) {
        $response = $client.GetAsync("$BaseUrl$route").GetAwaiter().GetResult()
        if ([int]$response.StatusCode -ne 401) { throw "$route deveria retornar 401 sem token; retornou $([int]$response.StatusCode)." }
        Write-Host "OK: $route protegido (401 sem token)." -ForegroundColor Green
    }
    Write-Host 'Próximo: testar os dois botões do frontend após login USER e ADMIN.'
} finally { $client.Dispose() }
