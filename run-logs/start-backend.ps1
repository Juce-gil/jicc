param([string]$Mvn,[string]$WorkDir,[string]$Log)
Set-Location $WorkDir
& $Mvn spring-boot:run *>> $Log
