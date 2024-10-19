package com.example.calculator

import android.os.Bundle
import android.text.Editable
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var showResult: EditText
    private var currentExpression: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        showResult = findViewById(R.id.showResult)

        // Initialize all buttons and set their click listeners
        val buttons = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
            R.id.btnplus, R.id.btnminus, R.id.btnmultiple, R.id.btndivision,
            R.id.btndot, R.id.btnModul
        )
        buttons.forEach { id ->
            findViewById<Button>(id).setOnClickListener { appendToExpression((it as Button).text.toString()) }
        }

        findViewById<Button>(R.id.btnAc).setOnClickListener { clearExpression() }
        findViewById<Button>(R.id.btnX).setOnClickListener { removeLastCharacter() }
        findViewById<Button>(R.id.btnC).setOnClickListener { clearExpression() }
        findViewById<Button>(R.id.equal).setOnClickListener { evaluateExpression() }
    }

    private fun appendToExpression(value: String) {
        currentExpression += value
        showResult.text = Editable.Factory.getInstance().newEditable(currentExpression)
    }

    private fun clearExpression() {
        currentExpression = ""
        showResult.text = Editable.Factory.getInstance().newEditable("0")
    }

    private fun removeLastCharacter() {
        if (currentExpression.isNotEmpty()) {
            currentExpression = currentExpression.dropLast(1)
            showResult.text = Editable.Factory.getInstance().newEditable(currentExpression)
        }
    }

    private fun evaluateExpression() {
        try {
            val result = eval(currentExpression)
            showResult.text = Editable.Factory.getInstance().newEditable(result.toString())
            currentExpression = result.toString()
        } catch (e: Exception) {
            showResult.text = Editable.Factory.getInstance().newEditable("Error")
            currentExpression = ""
        }
    }

    // Basic evaluation function
    private fun eval(expression: String): Double {
        return object :   Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < expression.length) expression[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < expression.length) throw RuntimeException("Unexpected: ${ch.toChar()}")
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    when {
                        eat('+'.code) -> x += parseTerm() // addition
                        eat('-'.code) -> x -= parseTerm() // subtraction
                        else -> return x
                    }
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    when {
                        eat('*'.code) -> x *= parseFactor() // multiplication
                        eat('/'.code) -> x /= parseFactor() // division
                        eat('%'.code) -> x %= parseFactor() // modulo
                        else -> return x
                    }
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor() // unary plus
                if (eat('-'.code)) return -parseFactor() // unary minus

                var x: Double
                val startPos = pos
                if (eat('('.code)) { // parentheses
                    x = parseExpression()
                    eat(')'.code)
                } else if (ch in '0'.code..'9'.code || ch == '.'.code) { // numbers
                    while (ch in '0'.code..'9'.code || ch == '.'.code) nextChar()
                    x = expression.substring(startPos, pos).toDouble()
                } else {
                    throw RuntimeException("Unexpected: ${ch.toChar()}")
                }

                return x
            }
        }.parse()
    }
}
