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
    $requests = @(
        @{ Method = [System.Net.Http.HttpMethod]::Get; Route = '/artigos' },
        @{ Method = [System.Net.Http.HttpMethod]::Post; Route = '/artigos' },
        @{ Method = [System.Net.Http.HttpMethod]::Delete; Route = '/artigos/1' }
    )
    foreach ($item in $requests) {
        $message = [System.Net.Http.HttpRequestMessage]::new($item.Method, "$BaseUrl$($item.Route)")
        $response = $client.SendAsync($message).GetAwaiter().GetResult()
        $label = "$($item.Method) $($item.Route)"
        if ([int]$response.StatusCode -ne 401) { throw "$label deveria retornar 401 sem token; retornou $([int]$response.StatusCode)." }
        Write-Host "OK: $label protegido (401 sem token)." -ForegroundColor Green
    }
    Write-Host 'Próximo: testar listar, criar e deletar artigos no frontend após login USER e ADMIN.'
} finally { $client.Dispose() }
