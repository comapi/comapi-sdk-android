$MyFile = Get-Content .\foundation\src\main\java\com\comapi\BaseComapi.java
$Utf8NoBomEncoding = New-Object System.Text.UTF8Encoding($False)
[System.IO.File]::WriteAllLines(".\foundation\src\main\java\com\comapi\BaseComapi.java", $MyFile, $Utf8NoBomEncoding)