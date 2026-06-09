package com.kotlin.mobileacademic

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kotlin.mobileacademic.ui.theme.MobileAcademicTheme

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setContent {

            MobileAcademicTheme {

                DashboardScreen(
                    auth = auth,
                    firestore = firestore,
                    onLogout = {
                        auth.signOut()

                        startActivity(
                            Intent(
                                this,
                                LoginActivity::class.java
                            )
                        )

                        finish()
                    }
                )
            }
        }
    }
}

data class Student(
    val nama: String = "",
    val nim: String = "",
    val prodi: String = "",
    val email: String = ""
)

data class Schedule(
    val hari: String = "",
    val mataKuliah: String = "",
    val jam: String = "",
    val ruang: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    auth: FirebaseAuth,
    firestore: FirebaseFirestore,
    onLogout: () -> Unit
)   {
    var student by remember {
        mutableStateOf(Student())
    }

    var schedules by remember {
        mutableStateOf(listOf<Schedule>())
    }

    LaunchedEffect(Unit) {

        val uid = auth.currentUser?.uid

        if (uid != null) {

            firestore.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->

                    if (document.exists()) {

                        student = Student(
                            nama = document.getString("nama") ?: "",
                            nim = document.getString("nim") ?: "",
                            prodi = document.getString("prodi") ?: "",
                            email = document.getString("email") ?: ""
                        )
                    }
                }

            firestore.collection("schedules")
                .get()
                .addOnSuccessListener { result ->

                    println("JUMLAH JADWAL = ${result.size()}")

                    val list = mutableListOf<Schedule>()

                    for (doc in result) {

                        list.add(
                            Schedule(
                                hari = doc.getString("hari") ?: "",
                                mataKuliah = doc.getString("mataKuliah") ?: "",
                                jam = doc.getString("jam") ?: "",
                                ruang = doc.getString("ruang") ?: ""
                            )
                        )
                    }

                    schedules = list
                }
                .addOnFailureListener {
                    println("GAGAL AMBIL JADWAL: ${it.message}")
                }
            }
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Mobile Academic")
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {

                        Text(
                            text = "Selamat Datang 👋",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = student.nama,
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Text("NIM: ${student.nim}")
                    }
                }
            }

            item {

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = "Program Studi",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(student.prodi)
                    }
                }
            }

            item {

                Text(
                    text = "Jadwal Kuliah",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            items(schedules) { schedule ->

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = schedule.mataKuliah,
                            style = MaterialTheme.typography.titleLarge
                        )

                        Text("Hari : ${schedule.hari}")
                        Text("Jam : ${schedule.jam}")
                        Text("Ruang : ${schedule.ruang}")
                    }
                }
            }

            item {

                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Logout")
                }
            }
        }
    }
}