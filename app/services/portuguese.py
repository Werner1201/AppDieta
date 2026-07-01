ACCENT_REPLACEMENTS = {
    "Acem": "Acém",
    "acai": "açaí",
    "abobora": "abóbora",
    "Basicos": "Básicos",
    "basicos": "básicos",
    "bisteca suina": "bisteca suína",
    "Acucar": "Açúcar",
    "acucar": "açúcar",
    "Agua": "Água",
    "agua": "água",
    "Cafe": "Café",
    "cafe": "café",
    "Camarao": "Camarão",
    "camarao": "camarão",
    "Codigo": "Código",
    "codigo": "código",
    "Contra-file": "Contrafilé",
    "Contra-File": "Contrafilé",
    "Feijao": "Feijão",
    "feijao": "feijão",
    "frances": "francês",
    "file": "filé",
    "Grao": "Grão",
    "grao": "grão",
    "Linguica": "Linguiça",
    "linguica": "linguiça",
    "Macarrao": "Macarrão",
    "maca": "maçã",
    "Mamao": "Mamão",
    "mamao": "mamão",
    "moida": "moída",
    "moido": "moído",
    "Moida": "Moída",
    "Moido": "Moído",
    "mucarela": "muçarela",
    "paçoca": "paçoca",
    "Pao": "Pão",
    "pao": "pão",
    "porcao": "porção",
    "Requeijao": "Requeijão",
    "requeijao": "requeijão",
    "Proteina": "Proteína",
    "proteina": "proteína",
    "pure": "purê",
    "rucula": "rúcula",
    "salmao": "salmão",
    "Sodio": "Sódio",
    "sodio": "sódio",
    "Tilapia": "Tilápia",
    "tilapia": "tilápia",
    "usuario": "usuário",
    "xicara": "xícara",
}


def accent_text(value: str) -> str:
    fixed = value
    for old, new in ACCENT_REPLACEMENTS.items():
        fixed = fixed.replace(old, new)
    return fixed


def aliases_with_accents(value: str) -> str:
    accented = accent_text(value)
    return value if accented == value else f"{value} {accented}"
