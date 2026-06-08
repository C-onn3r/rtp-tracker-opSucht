# OpSucht Job & RTP Buchhaltung 🧾🚀

**WICHTIGER HINWEIS VORAB:**
Dieses Projekt ist zu 100% vibecoded mit dem LLM *Gemini 3.5 Flash Thinking Erweitert*. Ich habe das Ding nicht selbst programmiert, sondern von der KI zusammenbauen lassen. Ich übernehme keinerlei Haftung für explodierende Konten, verpasste Payouts oder geschmolzene PCs! Ich stelle die Mod einfach nur bereit, weil ich nett bin. Wenn es läuft, läuft's – wenn nicht, dann nicht. 🤷‍♂️

---

## 🔥 Was kann die Mod?

Die Mod trackt deine Ausgaben für RTP (Random Teleport) und verrechnet sie automatisch mit deinen Einnahmen aus dem Jobcenter, damit du deine Finanzen auf OpSucht im Blick behältst.

### 🛠️ Befehle & Funktionen

* **`/RTPMenu`**
    Öffnet das Hauptfenster der Buchhaltung. Hier siehst du die Verrechnung deiner RTP-Kosten mit den Job-Einnahmen.
    
<img width="705" height="482" alt="grafik" src="https://github.com/user-attachments/assets/32b7f5d9-42be-42ff-8c7c-27ee5745dfe6" />


* **`/RTPMenu Nachtrag`**
    Sollte dazu dienen, alte Einträge aus deinem Bank-Menü nachträglich eintragen zu lassen. 
    *Disclaimer:* Funktioniert absolut nicht richtig. Ich war aber zu faul, das neu zu kompilieren und es ist mir ehrlich gesagt auch egal. Es bleibt jetzt einfach so drin – nutzt es auf eigene Gefahr!

* **`/RTPMenu debug`**
    Schaltet Chat-Benachrichtigungen ein/aus. Wenn aktiviert, zeigt es dir im Chat an, dass ein Payout oder ein RTP-Biom erfolgreich erfasst wurde. Standardmäßig deaktiviert (weil es sonst nervt).

---

## 📂 Manuelle Bearbeitung (Für Daten-Messis)

Die Mod speichert deine Historie in einer einfachen Textdatei. Wenn du Werte korrigieren oder händisch eintragen willst, kannst du die Datei einfach mit einem Texteditor bearbeiten.

* **Dateiname:** `rtp_tracker_history.txt`
* **Speicherort:** Im `.minecraft/config`-Ordner (oder wo auch immer dein Minecraft-Verzeichnis auf Windows/Linux/Mac liegt, keine Ahnung, sucht es euch selbst raus).

---

## 🐛 Bekannte Bugs & Features (Die eigentlich Bugs sind)

Da das Ding von einer KI zusammengeschustert wurde, gibt es ein paar Eigenheiten:

1.  **Verpasste Payouts:** Das Job-Geld wird über die Payout-Nachrichten im Chat erfasst. Wenn du genau in dem Moment AFK bist, dich teleportierst oder der Chat laggt, kann es passieren, dass das Payout **nicht** registriert wird. Leb damit oder rechne es händisch nach!
2.  Der gesamte `/RTPMenu Nachtrag`-Befehl (siehe oben).
