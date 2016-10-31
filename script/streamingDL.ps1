$filename = "C:\Users\Mathilde\Dev\streamingDL\liste_URL-Limitless-s01.txt"
$option = [System.StringSplitOptions]::RemoveEmptyEntries

Get-Content $filename | ForEach-Object {
  $dest, $src = $_ -split('|', 2, $option)

  write-host $_
  write-host $dest
  write-host $src

  Invoke-WebRequest $src -OutFile $dest

}
