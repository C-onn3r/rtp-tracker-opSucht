# ==============================================================================
# CONFIGURATION / EINSTELLUNGEN
# ==============================================================================
# Pfad zu deiner rtp_tracker_history.txt (Nutzername anpassen!)
$FilePath = "C:\Users\DEIN_NUTZERNAME\AppData\Roaming\.minecraft\config\rtp_tracker_history.txt"

# Deine Discord Webhook-URL hier einfügen
$WebhookUrl = "HIER_DEINEN_DISCORD_WEBHOOK_EINFÜGEN"
# ==============================================================================

# Prüfen, ob die Datei existiert
if (-not (Test-Path $FilePath)) {
    Write-Error "Fehler: Datei '$FilePath' wurde nicht gefunden!"
    exit
}

# Prüfen, ob der Webhook eingetragen wurde
if ($WebhookUrl -eq "HIER_DEINEN_DISCORD_WEBHOOK_EINFÜGEN" -or [string]::IsNullOrEmpty($WebhookUrl)) {
    Write-Error "Fehler: Bitte konfiguriere zuerst deine WEBHOOK_URL im Skript!"
    exit
}

# Daten einlesen und ungültige Zeilen (z.B. META) filtern
$lines = Get-Content $FilePath | Where-Object { $_ -match '^\d' }

if ($lines.Count -eq 0) {
    Write-Error "Fehler: Keine gültigen Daten in der Datei gefunden!"
    exit
}

# Kultur-Formate für deutsche Ausgabe definieren
$deCulture = [System.Globalization.CultureInfo]::CreateSpecificCulture("de-DE")
$invCulture = [System.Globalization.CultureInfo]::InvariantCulture

function Format-DeFloat($val) { return $val.ToString("N1", $deCulture) }
function Format-DeInt($val)   { return $val.ToString("N0", $deCulture) }

$TotalRTP = 0
$TotalLohn = 0.0
$TotalXP = 0.0

# All-Time Werte berechnen
foreach ($line in $lines) {
    $parts = $line.Split(',')
    if ($parts.Count -ge 4) {
        $TotalRTP  += [int]::Parse($parts[1].Trim(), $invCulture)
        $TotalLohn += [double]::Parse($parts[2].Trim(), $invCulture)
        $TotalXP   += [double]::Parse($parts[3].Trim(), $invCulture)
    }
}

# Heute-Werte berechnen (Letzte Zeile)
$lastParts = $lines[-1].Split(',')
$HeuteDatum = $lastParts[0].Trim()
$HeuteRTP   = [int]::Parse($lastParts[1].Trim(), $invCulture)
$HeuteLohn  = [double]::Parse($lastParts[2].Trim(), $invCulture)
$HeuteXP    = [double]::Parse($lastParts[3].Trim(), $invCulture)

$HeuteRTPKosten = $HeuteRTP * 25000
$HeuteBilanz    = $HeuteLohn - $HeuteRTPKosten

$TotalRTPKosten = $TotalRTP * 25000
$TotalBilanz    = $TotalLohn - $TotalRTPKosten

# Formatierung für die Discord-Anzeige
$HeuteRTPKostenFmt = if ($HeuteRTPKosten -gt 0) { "-" + (Format-DeInt $HeuteRTPKosten) + "$" } else { "0$" }
$HeuteLohnFmt      = (Format-DeFloat $HeuteLohn) + "$"
$HeuteBilanzFmt    = (if ($HeuteBilanz -ge 0) { "+" } else { "" }) + (Format-DeFloat $HeuteBilanz) + "$"
$HeuteXPFmt        = Format-DeFloat $HeuteXP

$TotalRTPFmt       = Format-DeInt $TotalRTP
$TotalRTPKostenFmt = if ($TotalRTPKosten -gt 0) { "-" + (Format-DeInt $TotalRTPKosten) + "$" } else { "0$" }
$TotalLohnFmt      = (Format-DeFloat $TotalLohn) + "$"
$TotalBilanzFmt    = (if ($TotalBilanz -ge 0) { "+" } else { "" }) + (Format-DeFloat $TotalBilanz) + "$"
$TotalXPFmt        = Format-DeFloat $TotalXP

$SendTime = (Get-Date).ToString("HH:mm")

# JSON Payload für Discord zusammenbauen
$payload = @{
    embeds = @(
        @{
            title = "📆 RTP-Statistik vom $HeuteDatum ($SendTime Uhr)"
            color = 3447003
            fields = @(
                @{
                    name = "─── HEUTE ───"
                    value = "🔄 **RTPs:** $HeuteRTP ($HeuteRTPKostenFmt)`n💰 **Lohn:** $HeuteLohnFmt`n📊 **Bilanz:** $HeuteBilanzFmt`n✨ **Job-XP:** $HeuteXPFmt"
                    inline = $false
                },
                @{
                    name = "─── ALL-TIME HISTORIE ───"
                    value = "🔄 **RTPs gesamt:** $TotalRTPFmt ($TotalRTPKostenFmt)`n💰 **Lohn gesamt:** $TotalLohnFmt`n📈 **Bilanz gesamt:** $TotalBilanzFmt`n✨ **Job-XP gesamt:** $TotalXPFmt"
                    inline = $false
                }
            )
            footer = @{
                text = "Stündliches automatisches Update (Windows)"
            }
        }
    )
} | ConvertTo-Json -Depth 4

# Payload in UTF8 konvertieren (wichtig für Umlaute/Symbole) und senden
$bytes = [System.Text.Encoding]::UTF8.GetBytes($payload)
Invoke-RestMethod -Uri $WebhookUrl -Method Post -Body $bytes -ContentType "application/json; charset=utf-8"
