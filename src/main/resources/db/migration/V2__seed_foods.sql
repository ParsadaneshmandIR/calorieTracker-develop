-- Iranian dish nutrition is loaded idempotently from classpath CSV by FoodSeeder
-- (upsert on source + source_id). This migration reserves the V2 slot for any
-- SQL-only reference data and documents the raw-vs-cooked rule in-schema.

-- Marker comment for operators:
-- RAW rice (برنج خام) and COOKED rice (برنج پخته) must remain separate Food rows.
-- Never collapse them; kcal/100g differs by ~2.7× because of water absorption.

SELECT 1;
