---
version: 1
slug: "frontend-src-views-mapview-vue"
primary_target: "frontend/src/views/MapView.vue"
related_targets: ["frontend/src/views/ClinicCabinetView.vue","frontend/src/style.css"]
---

# Map surface

Mode: Operate. Primary target: `frontend/src/views/MapView.vue`. Related: `frontend/src/views/ClinicCabinetView.vue`, `frontend/src/views/LoginView.vue`, `frontend/src/views/RegisterView.vue`, `frontend/src/style.css`.

## Audience and job

Patient compares clinics by the price of one service, on a phone, and opens the clinic he wants. Clinic keeps its price list current. Success: the price of the chosen service is readable without a click, and the map confirms place.

## Direction contract

THESIS: The clinic list is a patient record sheet, not a card list. Fields are ruled, the price sits in a money column, and the map is the sheet's drawing. The category default — rounded cards over a pin map — is refused.

OWN-WORLD: Form stock `#e8ece6`, sheet `#fbfcfa`, blue-black ink `#1a2a2e`, rule `#c3ccc6`, clinical navy `#1f4e63`, stamp red `#a23b2a` for errors only. Golos Text, self-hosted, 400/500/700. 2px radius, 1px rules, dotted leaders in price rows. No shadows, no rounded cards, no serif.

STORY: The visitor reads down a ruled sheet: clinic, address, then service with its price on a dotted leader. One row selects, the map answers. The price column is what he remembers.

FIRST VIEWPORT: Document bar on top with two field labels and controls. Left column 356px: heading `Клиники`, count, then ruled rows with name and `4,0 из 5` on one baseline, address, price lines, review count. Right: the map, flush, framed by the column rule.

FORM: position 3 of my grounded list (price board, odontogram, patient record form, appointment slip, x-ray, enamel plate, drug leaflet); seed: fb368758a127dbef.

FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance.

## Constraints and open decisions

Demo data only. Keep semantics, keyboard access, and the existing API. Motion: one reveal of the list rows. Open: whether the clinic cabinet gets its own sheet composition beyond shared tokens.
