package com.example.calculimc

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private var imc: Double = 0.0
    private var categorie: String = ""
    private val historique = ArrayList<String>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editTextPoids = findViewById<EditText>(R.id.editTextPoids)
        val editTextTaille = findViewById<EditText>(R.id.editTextTaille)
        val buttonCalculer = findViewById<Button>(R.id.buttonCalculer)
        val buttonEffacer = findViewById<Button>(R.id.buttonEffacer)
        val buttonPartager = findViewById<Button>(R.id.buttonPartager)
        val textViewImc = findViewById<TextView>(R.id.textViewImc)
        val textViewCategorie = findViewById<TextView>(R.id.textViewCategorie)
        val progressBarImc = findViewById<ProgressBar>(R.id.progressBarImc)
        val listViewHistorique = findViewById<ListView>(R.id.listViewHistorique)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, historique)
        listViewHistorique.adapter = adapter

        buttonCalculer.setOnClickListener {
            val poidsStr = editTextPoids.text.toString()
            val tailleStr = editTextTaille.text.toString()

            if (poidsStr.isEmpty() || tailleStr.isEmpty()) {
                Toast.makeText(this, getString(R.string.erreur_champs), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val poids = poidsStr.toDoubleOrNull()
            var taille = tailleStr.toDoubleOrNull()

            if (poids == null || taille == null || poids <= 0 || taille <= 0) {
                Toast.makeText(this, getString(R.string.erreur_valeurs), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Conversion cm -> m si > 3
            if (taille > 3) taille /= 100

            imc = poids / (taille * taille)
            val imcArrondi = String.format(Locale.US, "%.2f", imc)

            textViewImc.text = getString(R.string.imc_result, imcArrondi)

            when {
                imc < 18.5 -> {
                    categorie = getString(R.string.categorie_insuffisance)
                    textViewCategorie.text = categorie
                    textViewCategorie.setTextColor(getColor(android.R.color.holo_orange_dark))
                }
                imc < 25 -> {
                    categorie = getString(R.string.categorie_normale)
                    textViewCategorie.text = categorie
                    textViewCategorie.setTextColor(getColor(android.R.color.holo_green_dark))
                }
                imc < 30 -> {
                    categorie = getString(R.string.categorie_surpoids)
                    textViewCategorie.text = categorie
                    textViewCategorie.setTextColor(getColor(android.R.color.holo_orange_dark))
                }
                imc < 35 -> {
                    categorie = getString(R.string.categorie_obesite_moderee)
                    textViewCategorie.text = categorie
                    textViewCategorie.setTextColor(getColor(android.R.color.holo_red_dark))
                }
                imc < 40 -> {
                    categorie = getString(R.string.categorie_obesite_severe)
                    textViewCategorie.text = categorie
                    textViewCategorie.setTextColor(getColor(android.R.color.holo_red_dark))
                }
                else -> {
                    categorie = getString(R.string.categorie_obesite_morbide)
                    textViewCategorie.text = categorie
                    textViewCategorie.setTextColor(getColor(android.R.color.holo_red_light))
                }
            }

            // Jauge colorée
            progressBarImc.progress = imc.toInt().coerceAtMost(50)

            // Historique
            historique.add("Poids: $poids kg, Taille: $taille m → IMC: $imcArrondi ($categorie)")
            adapter.notifyDataSetChanged()
        }

        buttonEffacer.setOnClickListener {
            editTextPoids.text.clear()
            editTextTaille.text.clear()
            textViewImc.text = ""
            textViewCategorie.text = ""
            progressBarImc.progress = 0
            editTextPoids.requestFocus()
        }

        buttonPartager.setOnClickListener {
            if (imc > 0) {
                val imcArrondi = String.format(Locale.US, "%.2f", imc)
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Mon IMC est $imcArrondi : $categorie")
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(shareIntent, "Partager via"))
            } else {
                Toast.makeText(this, "Aucun résultat à partager", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Sauvegarde du dernier résultat après rotation
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putDouble("imc", imc)
        outState.putString("categorie", categorie)
        outState.putStringArrayList("historique", historique)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        imc = savedInstanceState.getDouble("imc")
        categorie = savedInstanceState.getString("categorie", "")
        val textViewImc = findViewById<TextView>(R.id.textViewImc)
        val textViewCategorie = findViewById<TextView>(R.id.textViewCategorie)
        val progressBarImc = findViewById<ProgressBar>(R.id.progressBarImc)
        val listViewHistorique = findViewById<ListView>(R.id.listViewHistorique)

        if (imc > 0) {
            val imcArrondi = String.format(Locale.US, "%.2f", imc)
            textViewImc.text = getString(R.string.imc_result, imcArrondi)
            textViewCategorie.text = categorie
            progressBarImc.progress = imc.toInt().coerceAtMost(50)
        }

        val savedHistorique = savedInstanceState.getStringArrayList("historique")
        if (savedHistorique != null) {
            historique.clear()
            historique.addAll(savedHistorique)
            adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, historique)
            listViewHistorique.adapter = adapter
        }
    }
}
