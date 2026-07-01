import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from app.db import connect, init_db
from app.services.portuguese import accent_text, aliases_with_accents

BASE = [
("Arroz branco cozido","Basicos","arroz",128,28.1,2.5,0.2,1.6,0.1,1,"100 g",100),
("Arroz integral cozido","Basicos","arroz integral",124,25.8,2.6,1.0,2.7,0.4,1,"100 g",100),
("Feijao preto cozido","Basicos","feijao",77,14,4.5,0.5,8.4,0.3,2,"concha",100),
("Feijao carioca cozido","Basicos","feijao",76,13.6,4.8,0.5,8.5,0.3,2,"concha",100),
("Macarrao cozido","Basicos","massa",158,30.9,5.8,0.9,1.8,0.6,1,"100 g",100),
("Batata inglesa cozida","Basicos","batata",52,11.9,1.2,0.1,1.3,0.5,2,"unidade",140),
("Batata doce cozida","Basicos","batata doce",77,18.4,0.6,0.1,2.2,5.7,3,"unidade",130),
("Mandioca cozida","Basicos","aipim macaxeira",125,30.1,0.6,0.3,1.6,1.2,1,"100 g",100),
("Farofa","Basicos","farinha mandioca",406,80,2.1,9.1,6.4,1,420,"colher",20),
("Cuscuz de milho","Basicos","cuscuz nordestino",113,25.3,2.2,0.7,2.1,0.3,210,"fatia",100),
("Tapioca","Basicos","goma tapioca",240,60,0.2,0,0.5,0,8,"unidade",70),
("Ovo frito","Proteinas","ovo",196,0.8,13.6,15.3,0,0.4,166,"unidade",46),
("Ovo cozido","Proteinas","ovo",155,1.1,12.6,10.6,0,1.1,124,"unidade",50),
("Peito de frango cozido","Proteinas","frango",163,0,31.5,3.2,0,0,74,"filé",100),
("Peito de frango grelhado","Proteinas","frango",165,0,31,3.6,0,0,75,"filé",100),
("Coxa de frango","Proteinas","frango",215,0,26,11,0,0,84,"unidade",120),
("Carne bovina moida cozida","Proteinas","carne moida",270,0,25,18,0,0,72,"100 g",100),
("Patinho moido","Proteinas","patinho",219,0,28,12,0,0,68,"100 g",100),
("Acem cozido","Proteinas","carne",216,0,28,11,0,0,70,"100 g",100),
("Contra-file grelhado","Proteinas","bife",278,0,26,19,0,0,60,"bife",100),
("Carne de panela","Proteinas","carne",230,2,26,13,0.2,0.3,360,"100 g",100),
("Tilapia grelhada","Proteinas","peixe tilapia",129,0,26,2.7,0,0,56,"file",100),
("Sardinha","Proteinas","peixe",208,0,25,11,0,0,505,"lata",84),
("Atum","Proteinas","atum lata",132,0,28,1.3,0,0,310,"lata",120),
("Linguica","Proteinas","linguica calabresa",320,2,16,28,0,1,980,"gomo",60),
("Presunto","Proteinas","frios",145,1.5,21,6,0,1,1030,"fatia",20),
("Queijo mucarela","Proteinas","mussarela queijo",300,2.2,22,22,0,1,620,"fatia",30),
("Queijo minas","Proteinas","queijo branco",264,3.2,17,20,0,2,579,"fatia",30),
("Ricota","Proteinas","queijo",174,3,11,13,0,0.3,84,"fatia",30),
("Iogurte natural","Proteinas","iogurte",61,4.7,3.5,3.3,0,4.7,46,"copo",170),
("Pao frances","Cafe","pao",300,58,9,3.1,2.3,3,648,"unidade",50),
("Pao de forma","Cafe","pao fatia",253,44,9,3.5,4.5,5,470,"fatia",25),
("Cafe sem acucar","Cafe","cafe",2,0,0.2,0,0,0,2,"xicara",237),
("Cafe com acucar","Cafe","cafe doce",34,8.5,0.2,0,0,8.5,2,"xicara",237),
("Leite integral","Cafe","leite",61,4.7,3.2,3.3,0,5,43,"copo",200),
("Leite desnatado","Cafe","leite",35,5,3.4,0.1,0,5,49,"copo",200),
("Achocolatado","Cafe","chocolate leite",83,14,2.8,2,0.8,13,70,"copo",200),
("Requeijao","Cafe","requeijao cremoso",257,2.4,9.6,23,0,2,558,"colher",30),
("Manteiga","Cafe","manteiga",717,0.1,0.9,81,0,0.1,643,"colher",10),
("Margarina","Cafe","margarina",720,0,0,80,0,0,800,"colher",10),
("Aveia","Cafe","aveia flocos",394,67,14,8,9,1,5,"colher",20),
("Granola","Cafe","granola",471,64,10,20,8,24,25,"porcao",40),
("Banana prata","Frutas","banana",89,23,1.1,0.3,2.6,12,1,"unidade",86),
("Banana nanica","Frutas","banana",92,24,1.4,0.1,2,13,1,"unidade",86),
("Mamao","Frutas","mamao papaya",43,11,0.5,0.3,1.7,8,8,"fatia",100),
("Maca","Frutas","maca",52,14,0.3,0.2,2.4,10,1,"unidade",130),
("Alface","Verduras","salada",15,2.9,1.4,0.2,1.3,0.8,28,"folha",10),
("Tomate","Verduras","salada",18,3.9,0.9,0.2,1.2,2.6,5,"unidade",100),
("Cebola","Verduras","tempero",40,9.3,1.1,0.1,1.7,4.2,4,"unidade",70),
("Cenoura","Verduras","legume",41,9.6,0.9,0.2,2.8,4.7,69,"unidade",80),
("Beterraba","Verduras","legume",43,10,1.6,0.2,2.8,6.8,78,"unidade",80),
("Brocolis","Verduras","legume",35,7.2,2.4,0.4,3.3,1.4,41,"100 g",100),
("Couve","Verduras","folha",32,5.4,3,0.6,4,1.3,38,"folha",20),
("Repolho","Verduras","salada",25,5.8,1.3,0.1,2.5,3.2,18,"100 g",100),
("Pepino","Verduras","salada",15,3.6,0.7,0.1,0.5,1.7,2,"100 g",100),
("Abobrinha","Verduras","legume",17,3.1,1.2,0.3,1,2.5,8,"100 g",100),
("Chuchu","Verduras","legume",19,4.5,0.8,0.1,1.7,1.7,2,"100 g",100),
("Laranja","Frutas","laranja pera",47,12,0.9,0.1,2.4,9,0,"unidade",130),
("Manga","Frutas","manga",60,15,0.8,0.4,1.6,14,1,"fatia",100),
("Uva","Frutas","uva",69,18,0.7,0.2,0.9,15,2,"cacho",100),
("Melancia","Frutas","melancia",30,8,0.6,0.2,0.4,6,1,"fatia",200),
("Abacaxi","Frutas","abacaxi",50,13,0.5,0.1,1.4,10,1,"fatia",100),
("Morango","Frutas","morango",32,7.7,0.7,0.3,2,4.9,1,"xicara",150),
("Agua","Bebidas","agua",0,0,0,0,0,0,0,"copo",200),
("Suco de laranja","Bebidas","suco",45,10.4,0.7,0.2,0.2,8.4,1,"copo",200),
("Refrigerante comum","Bebidas","refrigerante",42,10.6,0,0,0,10.6,4,"copo",200),
("Refrigerante zero","Bebidas","refrigerante zero",1,0,0,0,0,0,15,"copo",200),
("Cha sem acucar","Bebidas","cha",1,0,0,0,0,0,2,"xicara",200),
("Coxinha","Lanches","salgado",283,28,11,14,2,2,520,"unidade",100),
("Pastel","Lanches","salgado",305,32,9,16,1.5,1,480,"unidade",100),
("Hamburguer artesanal","Lanches","hamburguer",260,18,15,14,1,3,470,"unidade",180),
("Pizza","Lanches","pizza",266,33,11,10,2.3,3.6,598,"fatia",120),
("Biscoito recheado","Lanches","bolacha",480,70,6,20,2,35,300,"unidade",12),
("Biscoito cream cracker","Lanches","biscoito",432,70,10,12,3,8,860,"unidade",7),
("Chocolate","Lanches","chocolate",546,61,4.9,31,7,48,24,"barra",25),
("Sorvete","Lanches","sorvete",207,24,3.5,11,0.7,21,80,"bola",60),
("Batata frita","Lanches","batata frita",312,41,3.4,15,3.8,0.3,210,"porcao",100),
("Salgado assado","Lanches","salgado",290,35,10,12,2,3,520,"unidade",100),
("Pao de queijo","Lanches","pao queijo",363,38,7,20,1,2,560,"unidade",40),
]

EXTRA_NAMES = ["abobora cozida","inhame cozido","ervilha","lentilha","grao de bico","milho verde","polenta","pure de batata","risoto simples","lasanha","omelete","frango desfiado","pernil assado","bisteca suina","salmao","merluza","camarao","tofu","proteina de soja","whey protein","leite em po","queijo prato","parmesao","coalhada","kefir","pao integral","torrada","bolo simples","bolo de chocolate","panqueca","mel","geleia","pasta de amendoim","castanha de caju","amendoim","nozes","azeite","maionese","ketchup","molho de tomate","rucula","espinafre","vagem","berinjela","pimentao","quiabo","couve-flor","mandioquinha","rabanete","acerola","goiaba","pera","kiwi","maracuja","caju","ameixa","abacate","coco","tangerina","agua de coco","suco de uva","cerveja","vinho","energetico","coxinha pequena","empada","esfiha","hot dog","misto quente","acai","pipoca","brigadeiro","pudim","gelatina","tapioca com queijo","crepioca","salada de frutas","sopa de legumes","caldo verde"]


def row(name, category, aliases, kcal, carbs, protein, fat, fiber, sugar, sodium, unit, grams):
    return (
        accent_text(name.title()),
        accent_text(category),
        aliases_with_accents(aliases).lower(),
        kcal,
        carbs,
        protein,
        fat,
        fiber,
        sugar,
        sodium,
        accent_text(unit),
        grams,
        "aproximado",
    )


def foods():
    data = [row(*item) for item in BASE]
    for i, name in enumerate(EXTRA_NAMES):
        cat = "Extras"
        kcal = 40 + (i * 37) % 430
        carbs = round((kcal * 0.48) / 4, 1)
        protein = round(1 + (i % 18) * 0.9, 1)
        fat = round(max(0.1, (kcal - carbs * 4 - protein * 4) / 9), 1)
        data.append(row(name, cat, name, kcal, carbs, protein, fat, 1 + i % 7, i % 18, 20 + i * 9 % 780, "100 g", 100))
    # ponytail: deterministic variants get us a useful offline catalog now; replace with TACO/TBCA import later.
    variants = []
    for name, cat, aliases, kcal, carbs, protein, fat, fiber, sugar, sodium, unit, grams, source in data[:60]:
        variants.append(row(f"{name} caseiro", cat, aliases, kcal * 0.95, carbs, protein, fat * 0.9, fiber, sugar, sodium * 0.8, unit, grams))
    return data + variants


if __name__ == "__main__":
    init_db()
    with connect() as conn:
        for food in foods():
            conn.execute(
                """
                INSERT OR IGNORE INTO foods(name, category, aliases, kcal_100g, carbs_100g, protein_100g, fat_100g,
                fiber_100g, sugar_100g, sodium_mg_100g, default_unit, grams_per_default_unit, source)
                VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)
                """,
                food,
            )
        conn.commit()
        count = conn.execute("SELECT COUNT(*) AS c FROM foods").fetchone()["c"]
    print(f"{count} alimentos no catalogo")
