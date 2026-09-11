package com.iptvplayer.app.data.model

import java.text.Normalizer
import java.util.Locale

/**
 * Résout les logos de chaîne. Comme beaucoup de panels Xtream fournissent
 * des URLs de logo cassées ou mortes, on ne fait plus confiance à une seule
 * source : candidateUrls() renvoie une liste ordonnée (logo du panel, puis
 * logo de secours depuis le repo communautaire tv-logo/tv-logos) que l'UI
 * essaie une par une, en passant au suivant si le chargement échoue.
 * Si toutes échouent, l'UI affiche un badge à initiales.
 *
 * Pour ajouter une chaîne : trouve son fichier sur
 * https://github.com/tv-logo/tv-logos/tree/main/countries/france
 * et ajoute une ligne "mot-clé" to "nom-du-fichier.png" ci-dessous.
 * Mets les mots-clés les plus précis AVANT les plus génériques
 * (ex: "bein sports 1" avant "bein sports").
 */
object ChannelLogos {

    private const val BASE_URL = "https://raw.githubusercontent.com/tv-logo/tv-logos/main/countries/france/"

    private val knownLogos = linkedMapOf(
        // Généralistes
        "tf1 series films" to "tf1-series-films-fr.png",
        "tf1 plus" to "tf1-plus-fr.png",
        "tf1" to "tf1-fr.png",
        "france 2" to "france-2-fr.png",
        "france 3" to "france-3-fr.png",
        "france 4" to "france-4-fr.png",
        "france 5" to "france-5-fr.png",
        "m6 music" to "m6-music-fr.png",
        "m6" to "m6-fr.png",
        "w9" to "w9-fr.png",
        "tmc plus" to "tmc-plus-fr.png",
        "tmc" to "tmc-fr.png",
        "tfx" to "tfx-fr.png",
        "nrj 12" to "nrj-12-fr.png",
        "nrj hits" to "nrj-hits-fr.png",
        "c8" to "c8-fr.png",
        "c star" to "c-star-fr.png",
        "rtl9" to "rtl9-fr.png",
        "gulli" to "gulli-fr.png",
        "arte" to "arte-fr.png",
        "6ter" to "6ter-fr.png",
        "novo19" to "novo19-fr.png",
        "t18" to "t18-fr.png",
        "ol tv" to "ol-tv-fr.png",
        "one tv" to "one-tv-fr.png",

        // Infos
        "bfm business" to "bfm-business-fr.png",
        "bfm paris" to "bfm-paris-fr.png",
        "bfm lyon" to "bfm-lyon-fr.png",
        "bfm grand lille" to "bfm-grand-lille-fr.png",
        "bfm grand littoral" to "bfm-grand-littoral-fr.png",
        "bfm tv" to "bfm-tv-fr.png",
        "bfm" to "bfm-tv-fr.png",
        "cnews prime" to "c-news-prime-fr.png",
        "c news prime" to "c-news-prime-fr.png",
        "cnews" to "c-news-fr.png",
        "c news" to "c-news-fr.png",
        "lci" to "lci-fr.png",
        "franceinfo" to "franceinfo-fr.png",
        "france info" to "franceinfo-fr.png",
        "france 24" to "france-24-fr.png",
        "rt france" to "rt-france-fr.png",
        "public senat" to "public-senat-fr.png",
        "lcp" to "lcp-fr.png",
        "europe1" to "europe1-tv-fr.png",
        "europe 1" to "europe1-tv-fr.png",
        "europe 2 pop" to "europe-2-pop-tv-fr.png",
        "la chaine meteo" to "la-chaine-meteo-fr.png",

        // Canal+
        "canal plus box office" to "canal-plus-box-office-fr.png",
        "canal plus cinema" to "canal-plus-cinemas-fr.png",
        "canal plus docs" to "canal-plus-docs-fr.png",
        "canal plus foot" to "canal-plus-foot-fr.png",
        "canal plus formula1" to "canal-plus-formula1-fr.png",
        "canal plus formule 1" to "canal-plus-formula1-fr.png",
        "canal plus grand ecran" to "canal-plus-grand-ecran-fr.png",
        "canal plus hello" to "canal-plus-hello-fr.png",
        "canal plus kids" to "canal-plus-kids-fr.png",
        "canal plus ligue1" to "canal-plus-ligue1-fr.png",
        "canal plus magic" to "canal-plus-magic-fr.png",
        "canal plus moto gp" to "canal-plus-moto-gp-fr.png",
        "canal plus outremer" to "canal-plus-outremer-fr.png",
        "canal plus premier league" to "canal-plus-premier-league-fr.png",
        "canal plus pl" to "canal-plus-premier-league-fr.png",
        "canal plus series" to "canal-plus-series-fr.png",
        "canal plus sport 360" to "canal-plus-sport-360-fr.png",
        "canal plus sport" to "canal-plus-sport-fr.png",
        "canal plus story" to "canal-plus-story-fr.png",
        "canal plus top 14" to "canal-plus-top-14-rugby-fr.png",
        "canal plus" to "canal-plus-fr.png",
        "canal j" to "canal-j-fr.png",

        // beIN Sports
        "bein sports 1" to "bein-sports-1-french-fr.png",
        "bein sports 2" to "bein-sports-2-french-fr.png",
        "bein sports 3" to "bein-sports-3-french-fr.png",
        "bein sports" to "bein-sports-fr.png",

        // Ligue1+
        "ligue1 plus 10" to "ligue-1plus-10-fr.png",
        "ligue1 plus 2" to "ligue-1plus-2-fr.png",
        "ligue1 plus 3" to "ligue-1plus-3-fr.png",
        "ligue1 plus 4" to "ligue-1plus-4-fr.png",
        "ligue1 plus 5" to "ligue-1plus-5-fr.png",
        "ligue1 plus 6" to "ligue-1plus-6-fr.png",
        "ligue1 plus 7" to "ligue-1plus-7-fr.png",
        "ligue1 plus 8" to "ligue-1plus-8-fr.png",
        "ligue1 plus 9" to "ligue-1plus-9-fr.png",
        "ligue1 plus" to "ligue-1plus-fr.png",

        // RMC
        "rmc sport live 10" to "rmc-sport-live-10-fr.png",
        "rmc sport live 11" to "rmc-sport-live-11-fr.png",
        "rmc sport live 12" to "rmc-sport-live-12-fr.png",
        "rmc sport live 13" to "rmc-sport-live-13-fr.png",
        "rmc sport live 14" to "rmc-sport-live-14-fr.png",
        "rmc sport live 15" to "rmc-sport-live-15-fr.png",
        "rmc sport live 16" to "rmc-sport-live-16-fr.png",
        "rmc sport live 5" to "rmc-sport-live-5-fr.png",
        "rmc sport live 6" to "rmc-sport-live-6-fr.png",
        "rmc sport live 7" to "rmc-sport-live-7-fr.png",
        "rmc sport live 8" to "rmc-sport-live-8-fr.png",
        "rmc sport live 9" to "rmc-sport-live-9-fr.png",
        "rmc sport access 1" to "rmc-sport-access-1-fr.png",
        "rmc sport access 2" to "rmc-sport-access-2-fr.png",
        "rmc sport access 3" to "rmc-sport-access-3-fr.png",
        "rmc sport news" to "rmc-sport-news-fr.png",
        "rmc sport 1" to "rmc-sport-1-fr.png",
        "rmc sport 2" to "rmc-sport-2-fr.png",
        "rmc sport 3" to "rmc-sport-3-fr.png",
        "rmc sport 4" to "rmc-sport-4-fr.png",
        "rmc sport" to "rmc-sport-fr.png",
        "rmc decouverte" to "rmc-decouverte-fr.png",
        "rmc life" to "rmc-life-fr.png",
        "rmc story" to "rmc-story-fr.png",

        // Sport
        "eurosport 1" to "eurosport-1-fr.png",
        "eurosport 2" to "eurosport-2-fr.png",
        "multisports 1" to "multisports-1-fr.png",
        "multisports 2" to "multisports-2-fr.png",
        "multisports 3" to "multisports-3-fr.png",
        "multisports 4" to "multisports-4-fr.png",
        "multisports 5" to "multisports-5-fr.png",
        "multisports 6" to "multisports-6-fr.png",
        "multisports" to "multisports-fr.png",
        "infosport" to "infosport-plus-fr.png",
        "golf plus" to "golf-plus-fr.png",
        "equidia" to "equidia-fr.png",
        "lequipe" to "lequipe-fr.png",
        "l equipe" to "lequipe-fr.png",
        "trace sport stars" to "trace-sport-stars-fr.png",
        "motorvision plus" to "motorvision-plus-fr.png",
        "motorvision" to "motorvision-tv-fr.png",
        "sport en france" to "sport-en-france-fr.png",

        // Cinéma / séries
        "cine plus classic" to "cine-plus-classic-fr.png",
        "cine plus emotion" to "cine-plus-emotion-fr.png",
        "cine plus family" to "cine-plus-family-fr.png",
        "cine plus festival" to "cine-plus-festival-fr.png",
        "cine plus frisson" to "cine-plus-frisson-fr.png",
        "cine plus ocs" to "cine-plus-ocs-fr.png",
        "cine plus premier" to "cine-plus-premier-fr.png",
        "ocs choc" to "ocs-choc-fr.png",
        "ocs pulp" to "ocs-pulp-fr.png",
        "tcm cinema" to "tcm-cinema-fr.png",
        "tcm" to "tcm-cinema-fr.png",
        "serie club" to "serie-club-fr.png",
        "paris premiere" to "paris-premiere-fr.png",
        "comedie plus" to "comedie-plus-fr.png",
        "polar plus" to "polar-plus-fr.png",
        "crime district" to "crime-district-fr.png",
        "paramount channel decale" to "paramount-channel-decale-fr.png",
        "paramount channel" to "paramount-channel-fr.png",
        "teva" to "teva-fr.png",
        "planete plus aventure" to "planete-plus-aventure-fr.png",
        "planete plus crime" to "planete-plus-crime-fr.png",
        "planete plus" to "planete-plus-fr.png",
        "trek" to "trek-fr.png",
        "seasons" to "seasons-fr.png",
        "chasse et peche" to "chasse-et-peche-fr.png",
        "ushuaia" to "ushuaia-tv-fr.png",
        "ultra nature" to "ultra-nature-fr.png",
        "trace caribbean" to "trace-caribbean-fr.png",
        "trace latina" to "trace-latina-fr.png",
        "trace urban" to "trace-urban-fr.png",
        "toute l histoire" to "toute-lhistoire-fr.png",
        "toute lhistoire" to "toute-lhistoire-fr.png",
        "histoire" to "histoire-tv-fr.png",
        "tv5 monde" to "tv5-monde-fr.png",
        "tv5" to "tv5-monde-fr.png",
        "tv breizh" to "tv-breizh-fr.png",
        "novelas" to "novelas-tv-fr.png",
        "non stop people" to "non-stop-people-fr.png",
        "warner tv next" to "warner-tv-next-fr.png",
        "warner tv" to "warner-tv-fr.png",

        // Jeunesse
        "disney channel" to "disney-channel-fr.png",
        "disney jr" to "disney-jr-fr.png",
        "mon nickelodeon junior" to "mon-nickelodeon-junior-fr.png",
        "nickelodeon junior" to "nickelodeon-junior-fr.png",
        "nickelodeon plus" to "nickelodeon-plus-fr.png",
        "nickelodeon teen" to "nickelodeon-teen-fr.png",
        "nickelodeon" to "nickelodeon-fr.png",
        "cartoon network" to "cartoon-network-fr.png",
        "boomerang" to "boomerang-fr.png",
        "boing" to "boing-fr.png",
        "tiji" to "tiji-fr.png",
        "piwi plus" to "piwi-plus-fr.png",
        "pitchoun" to "pitchoun-tv-fr.png",
        "mangas" to "mangas-fr.png",
        "game one" to "game-one-fr.png",
        "teletoon plus" to "teletoon-plus-fr.png",

        // Musique / divers
        "mcm pop" to "mcm-pop-fr.png",
        "mcm top" to "mcm-top-fr.png",
        "mcm" to "mcm-fr.png",
        "mgg tv" to "mgg-tv-fr.png",
        "nolife" to "nolife-fr.png",
        "melody" to "melody-fr.png",
        "mezzo live" to "mezzo-live-fr.png",
        "mezzo" to "mezzo-fr.png",
        "stingray brava" to "stingray-brava-fr.png",
        "stingray djazz" to "stingray-djazz-fr.png",
        "culturebox" to "culturebox-fr.png",
        "science and vie" to "science-and-vie-tv-fr.png",
        "science et vie" to "science-and-vie-tv-fr.png",
        "dreamsee" to "dreamsee-fr.png",
        "drive in movie" to "drive-in-movie-channel-fr.png",
        "kto" to "kto-fr.png",
        "altice studio" to "altice-studio-fr.png",
        "animaux" to "animaux-fr.png",
        "olympia" to "olympia-tv-fr.png",
        "rfm tv" to "rfm-tv-fr.png",
        "j one" to "j-one-fr.png",
        "b smart" to "b-smart-fr.png",
        "bet" to "bet-fr.png",
        "action" to "action-fr.png",
        "abx" to "abx-fr.png",
        "ab1" to "ab1-fr.png",
        "ab3" to "ab3-fr.png",
        "automoto" to "automoto-la-chaine-fr.png"
    )

    /**
     * Renvoie la liste ordonnée des URLs à essayer pour cette chaîne :
     * d'abord le logo du panel (s'il existe), puis notre logo de secours.
     * L'UI doit essayer chaque URL dans l'ordre et passer à la suivante
     * si le chargement échoue.
     */
    fun candidateUrls(channelName: String, streamIconUrl: String?): List<String> {
        val urls = mutableListOf<String>()
        if (!streamIconUrl.isNullOrBlank()) urls += streamIconUrl
        lookup(channelName)?.let { urls += it }
        return urls
    }

    private fun lookup(channelName: String): String? {
        val normalized = normalize(channelName)
        val match = knownLogos.entries.firstOrNull { (keyword, _) -> normalized.contains(keyword) }
        return match?.let { BASE_URL + it.value }
    }

    private fun normalize(name: String): String {
        val withoutAccents = Normalizer.normalize(name, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
        return withoutAccents
            .lowercase(Locale.FRENCH)
            .replace("'", " ")
            .replace(Regex("[|#]"), " ")
            .replace("+", " plus ")
            .replace(Regex("ligue\\s*1"), "ligue1")
            .replace(Regex("\\bfr\\b"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
