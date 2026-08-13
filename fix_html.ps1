$f = "src\main\resources\static\dashboard.html"
$lines = [System.IO.File]::ReadAllLines($f)
$keep = $lines[0..979] + $lines[988..($lines.Length - 1)]
[System.IO.File]::WriteAllLines($f, $keep, [System.Text.Encoding]::UTF8)
Write-Host ("Done: " + $keep.Length + " lines")
