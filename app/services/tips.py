def tips(summary: dict, water_ml: int) -> list[str]:
    cfg = summary["settings"]
    totals = summary["totals"]
    out = []
    if totals["protein"] < cfg["daily_protein"] * 0.6:
        out.append("Proteina baixa hoje. Um jantar com frango, ovos ou feijao ajuda.")
    if water_ml < cfg["daily_water_ml"] * 0.5:
        out.append("Agua baixa. Comece com 250 ml agora.")
    if totals["fat"] > cfg["daily_fat"]:
        out.append("Gordura passou da meta. Prefira opcoes grelhadas no restante do dia.")
    if totals["carbs"] > cfg["daily_carbs"]:
        out.append("Carboidratos altos. Ajuste as proximas porcoes.")
    if totals["kcal"] and cfg["daily_kcal"] - totals["kcal"] < 150:
        out.append("Voce ficou perto da meta. Bom controle.")
    return out or ["Registre a proxima refeicao para manter o dia sob controle."]
