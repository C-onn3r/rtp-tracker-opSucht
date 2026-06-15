# OpSucht Job & RTP Buchhaltung 🧾🚀

---

## 🔥 Features & Befehle

Die Mod trackt deine RTP-Ausgaben (25.000$ pro Teleport) und verrechnet sie vollautomatisch und **in Echtzeit** mit deinen Einnahmen und Job-XP.

* **`/rtpmenu`** – Öffnet die grafische Buchhaltung (Übersicht aller Tage).
* **`/rtpmenu debug`** – Schaltet Chat-Benachrichtigungen für erfasste RTP-Biome an/aus.

<img width="855" height="488" alt="grafik" src="https://github.com/user-attachments/assets/5ec9e286-a3c0-4c9c-9340-234f00b8aaae"/>

---

## 📈 Datenerfassung

* **Live-Lohn & XP:** Wird direkt sekündlich aus der Actionbar ausgelesen. Kein Datenverlust bei Serverwechseln oder kaputten Werkzeugen.
* **Durchschnitt & Höchstwert:** Filtert automatisch Minuten-Payouts erst **ab 8.000$**, damit kurze Pausen oder Serverwechsel die Statistik nicht verfälschen.

---

## 📂 Speicherort & Dateien

Die Daten werden als einfache Textdateien unter `.minecraft/config/` gespeichert:
* **`rtp_tracker_history.txt`** – Enthält die täglichen Einträge im Format: `Datum, RTP-Anzahl, Lohn, Job-Xp`.
* **`rtp_all_payouts.txt`** – Protokoll aller einzelnen Minuten-Payouts für Daten-Messis.

---

## 📌 Discord-Statistiken einrichten (Stündliches Update)

Im Hauptordner findest du die Skripte `send_rtp_stats.sh` (Linux) und `send_rtp_stats.ps1` (Windows).

1️⃣ **Discord:** Kanaleinstellungen ➔ Integrationen ➔ Webhooks ➔ Webhook erstellen & URL kopieren.
2️⃣ **Skript:** Öffne die für dich passende Skript-Datei mit einem Texteditor und trage deine Webhook-URL sowie den Pfad zur `rtp_tracker_history.txt` ganz oben ein.

3️⃣ **Automatisieren:**
   * 🪟 **Windows:** In der "Aufgabenplanung" eine neue Aufgabe erstellen, die die `send_rtp_stats.ps1` stündlich startet.
   * 🐧 **Linux:** `crontab -e` öffnen und diese Zeile am Ende einfügen:  
     `0 * * * * /pfad/zu/send_rtp_stats.sh`