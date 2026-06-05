package com.prolearn.spar.constants

object Curriculum {
    val defaultTarget = "JEE Advanced"

    private val jeeData: Map<String, Map<String, List<String>>> = mapOf(
        "Physics" to mapOf(
            "Kinematics" to listOf(
                "v = u + at", "v\u00B2 = u\u00B2 + 2as", "s = ut + \u00BDat\u00B2",
                "Projectile motion", "Relative velocity", "Graphs of motion"
            ),
            "Laws of Motion" to listOf(
                "Newton's first law", "Newton's second law", "Newton's third law",
                "Free body diagrams", "Static friction", "Kinetic friction", "Pulley systems"
            ),
            "Work, Energy & Power" to listOf(
                "Work-energy theorem", "Conservative forces", "Potential energy",
                "Conservation of energy", "Power", "Collisions"
            ),
            "Electrostatics" to listOf(
                "Coulomb's law", "Electric field lines", "Gauss's law",
                "Electric potential", "Capacitors", "Energy stored in capacitor"
            ),
            "Current Electricity" to listOf(
                "Ohm's law", "Kirchhoff's laws", "Wheatstone bridge",
                "RC circuits", "EMF and internal resistance"
            ),
            "Magnetic Effects" to listOf(
                "Biot-Savart law", "Ampere's law", "Force on moving charge",
                "Cyclotron", "Torque on current loop"
            ),
            "Optics" to listOf(
                "Lens formula", "Mirror formula", "Total internal reflection",
                "Huygens principle", "Young's double slit"
            )
        ),
        "Chemistry" to mapOf(
            "Atomic Structure" to listOf(
                "Bohr model", "Quantum numbers", "Aufbau principle",
                "Pauli exclusion", "Hund's rule", "Electronic configuration"
            ),
            "Chemical Bonding" to listOf(
                "Ionic bonding", "Covalent bonding", "VSEPR theory",
                "Hybridization", "Molecular orbital theory", "Hydrogen bonding"
            ),
            "Thermodynamics" to listOf(
                "First law", "Enthalpy", "Hess's law", "Entropy",
                "Gibbs free energy", "Spontaneity"
            ),
            "Equilibrium" to listOf(
                "Le Chatelier's principle", "Kc and Kp", "pH calculations",
                "Buffer solutions", "Solubility product"
            ),
            "Organic Chemistry" to listOf(
                "IUPAC nomenclature", "Isomerism", "Reaction mechanisms",
                "Grignard reagent", "Aldol condensation"
            )
        ),
        "Mathematics" to mapOf(
            "Limits & Continuity" to listOf(
                "L'H\u00F4pital's rule", "Standard limits", "Left/right hand limits",
                "Continuity conditions", "Intermediate value theorem"
            ),
            "Differentiation" to listOf(
                "Chain rule", "Product rule", "Quotient rule",
                "Implicit differentiation", "Higher order derivatives"
            ),
            "Integration" to listOf(
                "Integration by parts", "Substitution", "Partial fractions",
                "Definite integrals", "Area under curves"
            ),
            "Matrices & Determinants" to listOf(
                "Matrix multiplication", "Inverse of matrix", "Cramer's rule",
                "Rank of matrix", "System of linear equations"
            ),
            "Probability" to listOf(
                "Conditional probability", "Bayes theorem", "Binomial distribution",
                "Expected value", "Variance"
            )
        ),
        "Biology" to mapOf(
            "Cell Biology" to listOf(
                "Cell organelles", "Cell membrane", "Mitosis", "Meiosis",
                "Cell cycle", "Apoptosis"
            ),
            "Genetics" to listOf(
                "Mendelian genetics", "Chromosomal theory", "DNA structure",
                "DNA replication", "Transcription", "Translation"
            ),
            "Human Physiology" to listOf(
                "Digestive system", "Respiratory system", "Circulatory system",
                "Nervous system", "Endocrine system"
            ),
            "Ecology" to listOf(
                "Food chains", "Biogeochemical cycles", "Ecological succession",
                "Biodiversity", "Conservation"
            )
        )
    )

    private val neetData: Map<String, Map<String, List<String>>> = mapOf(
        "Physics" to jeeData.getValue("Physics"),
        "Chemistry" to jeeData.getValue("Chemistry"),
        "Biology" to mapOf(
            "The Living World" to listOf("Taxonomy", "Binomial nomenclature", "Taxonomic hierarchy"),
            "Biological Classification" to listOf("Five kingdom classification", "Viruses", "Lichens"),
            "Plant Kingdom" to listOf("Algae", "Bryophytes", "Pteridophytes", "Gymnosperms", "Angiosperms"),
            "Animal Kingdom" to listOf("Basis of classification", "Non-chordates", "Chordates"),
            "Morphology of Flowering Plants" to listOf("Root", "Stem", "Leaf", "Inflorescence", "Flower"),
            "Anatomy of Flowering Plants" to listOf("Tissues", "Stem anatomy", "Root anatomy", "Secondary growth"),
            "Cell: The Unit of Life" to listOf("Cell theory", "Prokaryotic cells", "Eukaryotic organelles"),
            "Biomolecules" to listOf("Carbohydrates", "Proteins", "Lipids", "Enzymes"),
            "Cell Cycle and Cell Division" to listOf("Mitosis", "Meiosis", "Checkpoints"),
            "Photosynthesis in Higher Plants" to listOf("Light reaction", "Calvin cycle", "C4 pathway"),
            "Respiration in Plants" to listOf("Glycolysis", "Krebs cycle", "Electron transport chain"),
            "Human Physiology" to listOf("Digestion", "Breathing", "Circulation", "Excretion", "Neural control"),
            "Reproduction" to listOf("Flowering plant reproduction", "Human reproduction", "Reproductive health"),
            "Genetics and Evolution" to listOf("Mendelism", "Molecular basis", "Evolution"),
            "Biotechnology" to listOf("Recombinant DNA", "PCR", "Applications"),
            "Ecology" to listOf("Organisms and populations", "Ecosystem", "Biodiversity", "Environmental issues")
        )
    )

    private val upscData: Map<String, Map<String, List<String>>> = mapOf(
        "History" to mapOf(
            "Ancient India" to listOf("Indus Valley Civilization", "Vedic age", "Mauryan empire", "Gupta period"),
            "Medieval India" to listOf("Delhi Sultanate", "Bhakti and Sufi movements", "Mughal empire"),
            "Modern India" to listOf("British expansion", "1857 revolt", "National movement", "Gandhian era"),
            "World History" to listOf("Industrial revolution", "World wars", "Decolonisation", "Cold war"),
            "Art and Culture" to listOf("Architecture", "Paintings", "Classical dances", "Literature")
        ),
        "Polity" to mapOf(
            "Constitutional Framework" to listOf("Preamble", "Salient features", "Amendment procedure"),
            "Fundamental Rights and Duties" to listOf("Rights", "DPSP", "Duties"),
            "Union Government" to listOf("President", "Parliament", "Prime Minister", "Council of Ministers"),
            "State Government" to listOf("Governor", "State legislature", "Chief Minister"),
            "Judiciary" to listOf("Supreme Court", "High Courts", "Judicial review"),
            "Local Government" to listOf("Panchayati Raj", "Municipalities", "73rd and 74th amendments")
        ),
        "Geography" to mapOf(
            "Physical Geography" to listOf("Geomorphology", "Climatology", "Oceanography"),
            "Indian Geography" to listOf("Physiography", "Drainage", "Monsoon", "Soils"),
            "Human Geography" to listOf("Population", "Settlement", "Migration"),
            "Economic Geography" to listOf("Agriculture", "Industries", "Transport", "Resources"),
            "Environment" to listOf("Ecology", "Biodiversity", "Climate change", "Conservation")
        ),
        "Economy" to mapOf(
            "Basic Concepts" to listOf("GDP", "Inflation", "Fiscal policy", "Monetary policy"),
            "Planning and Growth" to listOf("Planning in India", "Inclusive growth", "Poverty"),
            "Banking and Finance" to listOf("RBI", "Money market", "Capital market", "NPAs"),
            "Public Finance" to listOf("Budget", "Taxation", "Deficits", "FRBM"),
            "External Sector" to listOf("Balance of payments", "Exchange rate", "Trade policy")
        ),
        "Science & Tech" to mapOf(
            "Space Technology" to listOf("ISRO missions", "Satellites", "Launch vehicles"),
            "Biotechnology" to listOf("Genetic engineering", "Vaccines", "Stem cells"),
            "Information Technology" to listOf("AI", "Cybersecurity", "Blockchain", "Quantum computing"),
            "Health and Disease" to listOf("Communicable diseases", "Immunisation", "Public health"),
            "Defence Technology" to listOf("Missiles", "Radar", "Cyber warfare")
        ),
        "Ethics" to mapOf(
            "Ethics and Human Interface" to listOf("Values", "Morality", "Ethical dilemmas"),
            "Attitude" to listOf("Content", "Structure", "Influence"),
            "Aptitude and Foundational Values" to listOf("Integrity", "Objectivity", "Empathy"),
            "Emotional Intelligence" to listOf("Self-awareness", "Self-regulation", "Social skills"),
            "Probity in Governance" to listOf("Transparency", "Accountability", "Citizen charter")
        )
    )

    val subjects: List<String> = subjectsForTarget(defaultTarget)

    fun subjectsForTarget(examTarget: String): List<String> =
        dataForTarget(examTarget).keys.toList()

    fun getChapters(subject: String, examTarget: String = defaultTarget): List<String> =
        dataForTarget(examTarget)[subject]?.keys?.toList() ?: emptyList()

    fun getConcepts(subject: String, chapter: String, examTarget: String = defaultTarget): List<String> =
        dataForTarget(examTarget)[subject]?.get(chapter) ?: emptyList()

    private fun dataForTarget(examTarget: String): Map<String, Map<String, List<String>>> {
        val normalized = examTarget.lowercase()
        return when {
            "neet" in normalized -> neetData
            "upsc" in normalized || "civil" in normalized -> upscData
            else -> jeeData
        }
    }
}
