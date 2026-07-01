import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from app.db import connect
from app.services.portuguese import accent_text, aliases_with_accents


if __name__ == "__main__":
    with connect() as conn:
        rows = conn.execute("SELECT id, name, category, aliases, default_unit, source FROM foods").fetchall()
        for row in rows:
            conn.execute(
                """
                UPDATE foods
                SET name=?, category=?, aliases=?, default_unit=?, source=?
                WHERE id=?
                """,
                (
                    accent_text(row["name"]),
                    accent_text(row["category"]),
                    aliases_with_accents(row["aliases"]).lower(),
                    accent_text(row["default_unit"]),
                    accent_text(row["source"]),
                    row["id"],
                ),
            )
        conn.commit()
        print(f"{len(rows)} alimentos normalizados")
