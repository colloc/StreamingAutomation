$filename = "C:\Users\Mathilde\Dev\streamingDL\liste_URL-Limitless-s01.txt"

Get-Content $filename | ForEach-Object {
  $dest, $src = $_ -split '|', 2;

  Invoke-WebRequest $src -OutFile $dest

}
