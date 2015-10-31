#$filename = "C:\Users\Mathilde\Dev\streamingDL\liste_URL-Limitless-s01.txt"
$filename = "D:\streaming\liste_URL-Ray_Donovan-s02.txt"

Get-Content $filename | ForEach-Object {
  $dest, $src = $_.split('|', 2)

  #$dest = $dest1 + ".mp4"

  Write-Host $dest
  Write-Host $src

  Invoke-WebRequest $src -OutFile $dest

}
