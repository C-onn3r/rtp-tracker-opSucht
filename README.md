# OpSucht Job & RTP Buchhaltung 🧾🚀

**WICHTIGER HINWEIS VORAB:**
Dieses Projekt ist zu 100% vibecoded mit dem LLM *Gemini*. Ich habe das Ding nicht selbst programmiert, sondern von der KI zusammenbauen lassen. Ich übernehme keinerlei Haftung für explodierende Konten, verpasste Payouts oder geschmolzene PCs! Ich stelle die Mod einfach nur bereit, weil ich nett bin. Wenn es läuft, läuft's – wenn nicht, dann nicht. 🤷‍♂️

---

## 🔥 Was kann die Mod?

Die Mod trackt deine Ausgaben für RTP (Random Teleport) und verrechnet sie vollautomatisch und **hochpräzise in Echtzeit** mit deinen Einnahmen und der Job-XP aus der Actionbar, damit du deine Finanzen und deinen Grind auf OpSucht perfekt im Blick behältst. 

### 🛠️ Befehle & Funktionen

* **`/rtpmenu`**
    Öffnet das Hauptfenster der Buchhaltung. Hier siehst du die tagesaktuelle Verrechnung deiner RTP-Kosten (25.000$ pro RTP) mit deinen Echtzeit-Einnahmen und deinen verdienten Job-XP.
    
<img width="855" height="488" alt="grafik" src="https://github.com/user-attachments/assets/5ec9e286-a3c0-4c9c-9340-234f00b8aaae" />


* **`/rtpmenu debug`**
    Schaltet Benachrichtigungen ein/aus. Wenn aktiviert, zeigt es dir im Chat an, wenn ein RTP-Biom erfasst wurde. Standardmäßig deaktiviert, um den Chat sauber zu halten.

---

## 📈 Intelligentes Payout- & Statistik-System

Die Mod nutzt zwei getrennte Systeme, um die Daten so präzise wie möglich zu erfassen:

1. **Echtzeit-Lohn & Job-XP (Actionbar):** Jeder einzelne Cent und jeder XP-Punkt werden direkt beim Farmen aus der Actionbar ausgelesen (`+11,17$ • +2,5 XP`). Das manuelle Eintragsfeld im Menü wurde komplett entfernt, da kein einziger Cent mehr verloren geht – selbst wenn du mitten in der Minute den Server wechselst oder dein Werkzeug kaputtgeht!
2. **Durchschnitt & Höchstwert (Chat):** Um den echten Minutendurchschnitt deines Jobs nicht durch Serverwechsel oder Pausen zu verfälschen, wird für das **Höchste Payout** und das **Durchschnitts-Payout** weiterhin das offizielle Minuten-Payout aus dem Chat abgefangen.
   * *Hinweis:* In die Durchschnitts-Berechnung fließen automatisch **nur Payouts ab 8.000$** ein, damit kleinere "Anfahrts-Minuten" oder unvollständige Intervalle die Statistik nicht verfälschen. Alle Payouts werden zudem ungefiltert in der Datei `rtp_all_payouts.txt` protokolliert.

---

## 📂 Manuelle Bearbeitung & Dateistruktur

Die Mod speichert deine Historie in einfachen Textdateien im Minecraft-Verzeichnis. Wenn du Werte korrigieren willst, kannst du sie einfach mit einem Texteditor bearbeiten.

* **Speicherort:** `.minecraft/config/`
* **`rtp_tracker_history.txt`:** Enthält die Metadaten (Höchstwerte, Durchschnitts-Zwischenstände) sowie die täglichen Einträge im Format: `Datum, RTP-Anzahl, Lohn, Job-XP`.
* **`rtp_all_payouts.txt`:** Eine fortlaufende Liste aller jemals erhaltenen Minuten-Chatpayouts für die ganz genauen Daten-Messis unter euch.

---

📌 **RTP-Tracker Discord Bot einrichten**

1️⃣ **Discord:** Kanaleinstellungen ➔ Integrationen ➔ Webhooks ➔ Neuen Webhook erstellen & URL kopieren.
2️⃣ **Skript:** Skript-Datei mit einem Texteditor öffnen, Webhook-URL und den Pfad zu deiner `rtp_tracker_history.txt` ganz oben eintragen.
3️⃣ **Automatisieren:**
   • **Windows:** In der "Aufgabenplanung" eine neue Aufgabe erstellen, die das Skript stündlich startet.
   • **Linux:** `crontab -e` öffnen und das hier eintragen: `0 * * * * /pfad/zu/send_rtp_stats.sh`
