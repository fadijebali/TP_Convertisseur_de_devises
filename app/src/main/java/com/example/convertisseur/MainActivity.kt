package com.example.convertisseur

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.convertisseur.databinding.ActivityMainBinding
import java.util.Locale

/**
 * Activité principale du convertisseur de devises.
 * Gère l'interface, la saisie via un clavier personnalisé et le calcul de conversion.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Taux de change fixes pour 1 EUR (Pivot)
    private val taux = mapOf(
        "EUR" to 1.0,
        "USD" to 1.17,
        "TND" to 3.40,
        "GBP" to 0.87,
        "JPY" to 172.50,
        "CAD" to 1.62
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialisation du View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinners()
        setupKeyboard()

        // Gestion du clic sur le bouton "CONVERTIR"
        binding.btnConvertir.setOnClickListener {
            val montantSaisi = binding.tvMontant.text.toString()
            val montant = montantSaisi.toDoubleOrNull() ?: 0.0

            if (montant == 0.0) {
                // Règle de gestion : afficher un Toast si le montant est nul
                Toast.makeText(this, getString(R.string.msg_saisir_montant), Toast.LENGTH_SHORT).show()
            } else {
                val source = binding.spDeviseSource.selectedItem.toString()
                val cible = binding.spDeviseCible.selectedItem.toString()
                
                val resultat = calculerConversion(montant, source, cible)
                
                // Affichage du résultat formaté à 2 décimales
                binding.tvResultat.text = String.format(Locale.US, "%.2f", resultat)
            }
        }

        // Gestion du bouton d'inversion des devises
        binding.btnInverser.setOnClickListener {
            val sourcePos = binding.spDeviseSource.selectedItemPosition
            val ciblePos = binding.spDeviseCible.selectedItemPosition
            
            // Échange des sélections dans les Spinners
            binding.spDeviseSource.setSelection(ciblePos)
            binding.spDeviseCible.setSelection(sourcePos)

            // Relancer la conversion si un résultat était déjà présent
            if (binding.tvResultat.text != "0.00") {
                binding.btnConvertir.performClick()
            }
        }
    }

    /**
     * Remplit les deux Spinners avec la liste des devises définie dans strings.xml.
     * Configure USD comme source et EUR comme cible par défaut.
     */
    private fun setupSpinners() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.devises,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        
        binding.spDeviseSource.adapter = adapter
        binding.spDeviseCible.adapter = adapter

        // Sélection par défaut : USD (index 0) et EUR (index 1)
        binding.spDeviseSource.setSelection(0)
        binding.spDeviseCible.setSelection(1)
    }

    /**
     * Configure les écouteurs de clics pour chaque touche du clavier numérique.
     */
    private fun setupKeyboard() {
        val listenerChiffres = android.view.View.OnClickListener { view ->
            val bouton = view as android.widget.Button
            ajouterCaractere(bouton.text.toString())
        }

        // Liste des touches numériques et du point
        val touches = listOf(
            binding.btn0, binding.btn1, binding.btn2, binding.btn3, binding.btn4,
            binding.btn5, binding.btn6, binding.btn7, binding.btn8, binding.btn9,
            binding.btnDot
        )
        
        touches.forEach { it.setOnClickListener(listenerChiffres) }

        // Touche 'C' pour effacer la saisie et le résultat
        binding.btnC.setOnClickListener {
            binding.tvMontant.text = "0"
            binding.tvResultat.text = "0.00"
        }
    }

    /**
     * Ajoute un caractère à la fin du TextView tvMontant en respectant les contraintes :
     * - Un seul point décimal autorisé.
     * - Limite de 10 caractères.
     * - Remplacement du "0" initial.
     */
    private fun ajouterCaractere(c: String) {
        var texteActuel = binding.tvMontant.text.toString()

        if (texteActuel.length >= 10) return
        if (c == "." && texteActuel.contains(".")) return

        if (texteActuel == "0" && c != ".") {
            texteActuel = c
        } else {
            texteActuel += c
        }

        binding.tvMontant.text = texteActuel
    }

    /**
     * Calcule la conversion entre deux devises en passant par l'Euro (EUR) comme pivot.
     */
    private fun calculerConversion(montant: Double, source: String, cible: String): Double {
        val tauxSource = taux[source] ?: 1.0
        val tauxCible = taux[cible] ?: 1.0

        // Etape 1 : Convertir le montant source vers l'Euro
        val montantEnEur = montant / tauxSource
        
        // Etape 2 : Convertir de l'Euro vers la devise cible
        return montantEnEur * tauxCible
    }
}
