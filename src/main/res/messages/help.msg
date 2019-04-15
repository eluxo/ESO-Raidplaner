__**Commands:**__


Hinweis: Alle Parameter enden beim nächsten Leerzeichen. Soll ein Feld Leerzeichen enthalten, muss es in Anführungsstriche gepackt werden.

__**Event erstellen:**__
`
- !new [Typ] [Name] [Datum] [Uhrzeit] [...]
         [Typ]: Art oder Typ des Events (raid, bossrun, pvp)
         [Name]: Eindeutiger Name des Events
         [Datum]: Datum formatiert als TT.MM.JJJJ
         [Uhrzeit]: Datum formatiert als HH:MM
         [...]: Eventspezifische Parameter
`

_**Raid:**_
`
- !new raid [Name] [Datum] [Uhrzeit] [Anzahl Tanks] [Anzahl Healer] [Anzahl DDs] <Beschreibung>
         Beispiel: !new raid vCR+3 11.11.2019 11:11 3 2 7 "Wir wollen uns nochmal an nCR+3 versuchen."
`
_**Worldboss Run:**_
`
 - !new bossrun [Name] [Datum] [Uhrzeit] <Beschreibung>
         Beispiel: !new bossrun Kargstein 11.11.2019 11:11 "Gruppenbosse in Kargstein"
`
_**PvP:**_
`
 - !new pvp [Name] [Datum] [Uhrzeit] [Allianz] <Beschreibung>
         [Allianz]: ebenerz, aldmeri oder dolchsturz
         Beispiel: !new pvp Kaiserstadt 11.11.2019 11:11 ebenerz "Dailies und Telvar-Farmrun in der Kaiserstadt"
`

Hinweis: Die <Beschreibung> ist optional.

__**Event beenden**__
`
 - !end [Name]
         Beispiel: !end vCR+3
`

__**Event bearbeiten**__
`
 - !mod [Name] [Datum] [Uhrzeit]
         Beispiel: !mod vCR+3 01.01.2022 01:22
`


__**Alle aktiven Events anzeigen**__
`
 - !eventlist
`

__**Angemeldete Teilnehmer anzeigen**__
`
 - !userlist [Name]
         [Name]: Eindeutiger Name des Events
         Beispiel: !userlist vCR+3
`

__**Hilfe anzeigen**__
`
 - !help
`

Der Quellcode für diesen Bot ist unter https://github.com/eluxo/ESO-Raidplaner zu finden.

Der Bot basiert auf einem Fork von https://github.com/DeityEXE/ESO-Raidplaner .
