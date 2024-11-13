package com.example.mystore

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.DatePicker
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.Calendar
import java.text.SimpleDateFormat
import java.text.ParseException
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.Image
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.collections.filter


class Billings : AppCompatActivity() {

    private val STORAGE_PERMISSION_REQUEST_CODE = 1001

    private lateinit var searchView: SearchView
    private lateinit var startDatePicker: DatePicker
    private lateinit var endDatePicker: DatePicker
    private lateinit var exportPdfButton: AppCompatButton
    private lateinit var billingEmail: TextView


    private lateinit var nameOfCustomer: String

    private lateinit var recyclerView: RecyclerView
//    private lateinit var adapter: RecycleAdapterBilling
    private var emailselect: String? = null

    //    private lateinit var searchView: SearchView
//    private lateinit var adapter: RecycleAdapterOrder
//    private var billings = ArrayList<Order>()
//
    private var orderHistoryss = ArrayList<OrderValue>()

    private var oorderHistorys: List<OrderValue> = emptyList()

    private var nameOfEmail: String? = null
//    private var originalOrders: List<Order> = emptyList()


    private var startDate: Date? = null
    private var endDate: Date? = null




    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(RetrofitInstance.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient())
            .build()
    }

    private val apiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_billings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val authInterceptor = AuthInterceptor(sharedPreferences)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .readTimeout(500, TimeUnit.SECONDS) // Set read timeout
            .connectTimeout(500, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(RetrofitInstance.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

        // Create a new apiService instance using the new Retrofit instance
        val apiServiceWithInterceptor = retrofit.create(ApiService::class.java)

        recyclerView = findViewById<RecyclerView>(R.id.recycleViewBilling)

        //Hide Recycler view











//        searchView = findViewById<SearchView>(R.id.searchTxt)

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

//        addDataToList()

//        adapter = RecycleAdapterBilling(billings)
//        recyclerView.adapter = adapter

//        adapter = RecycleAdapterBilling(originalOrders) { email ->
//            emailselect = email // Save clicked email to emailselect
//            // You can perform other actions here if needed
//        }
//        recyclerView.adapter = adapter

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

//        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                filterOrders(query)
//                return true
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                filterOrders(newText)
//                return true
//            }
//
//        })


//        recyclerView = findViewById<RecyclerView>(R.id.recycleViewBilling)

//        recyclerView.setHasFixedSize(true)
//        recyclerView.layoutManager = LinearLayoutManager(this)


        startDatePicker = findViewById(R.id.startDatePicker)
        endDatePicker = findViewById(R.id.endDatePicker)
        exportPdfButton = findViewById(R.id.exportBtn)

//        var startDateCalendar:Date? = null
//        var endDateCalendar = ""


        startDatePicker.setOnDateChangedListener { _, year, month, dayOfMonth ->

            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            startDate = calendar.time

//            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//            val formattedStartDate = formatter.format(startDate)
//            val formatterDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//            val fformattedStartDate = formatter.parse(formattedStartDate)
//            startDateCalendar = fformattedStartDate

            println("Start Date: $startDate")
        }

        endDatePicker.setOnDateChangedListener { _, year, month, dayOfMonth ->

            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            endDate = calendar.time

//            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//            val formattedEndDate = formatter.format(endDate)
//            val formatterDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//            val fformattedEndDate = formatter.parse(formattedEndDate)
//            endDateCalendar = fformattedEndDate

            println("End Date: $endDate")
        }


//        lifecycleScope.launch {
//            try {
//                val response = withContext(Dispatchers.IO) { apiServiceWithInterceptor.getOrders() }
//                if (response.isSuccessful) {
//                    println("Responce Sucessful: ${response.body()}")
//                    val customerResponse = response.body()
//                    val order = billings.addAll(customerResponse?.data ?: emptyList())
////                    val orders = customerResponse?.data ?: emptyList()
//                    val latestCustomers = billings.sortedByDescending { it.createdAt }
//                    println("Latest Orders are: $latestCustomers")
//
//                    originalOrders = latestCustomers // Store fetched data in originalOrders
//                    adapter = RecycleAdapterBilling(originalOrders) // Initialize adapter with original data
//                    recyclerView.adapter = adapter
//
//                    withContext(Dispatchers.Main) {
//                        adapter = RecycleAdapterBilling(latestCustomers)
//                        adapter.updateOrders(originalOrders) // Add fetched data to originalOrders
//                        recyclerView.adapter = adapter
//                    }
//
//
//                } else {
//                    // Handle API error
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(this@Billings, "API Error", Toast.LENGTH_SHORT).show()
//                    }
//                }
//
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(this@Billings, "Network Error", Toast.LENGTH_SHORT).show()
//                }
//
//                println("Checking Error Responce ")
//            }
//
//        }

//        val billingsAdapter = RecycleAdapterBilling(billings) { email ->
//            selectedCustomerEmail = email
//        }


        val email = intent.getStringExtra("email")
        val custemerEmail = email.toString()
        nameOfCustomer = custemerEmail

        val history = custemerEmail

//        val emalofBilling = findViewById<TextView>(R.id.emailOfBilling)
//        billingEmail.setText(custemerEmail)
//        val history = "mukesh@gmail.com"
        println("Email which coming from AllOrder: $history")


        lifecycleScope.launch {
            progressBar.visibility= View.VISIBLE
            try {
                val response =
                    withContext(Dispatchers.IO) { apiServiceWithInterceptor.getOrderHistory(EmailRequest(history))}
                if (response.isSuccessful) {
                    val customerResponse = response.body()
                    println("Responce Sucessful in OrderHistory : ${customerResponse}")
                    val order = customerResponse?.data

                    val orderValues = customerResponse?.data?.allOrders ?: emptyList()
                    orderHistoryss.addAll(orderValues)

                    val nameOfEmail = customerResponse?.data?.fullName
                    nameOfCustomer = customerResponse?.data?.fullName.toString()
                    val emailName = findViewById<TextView>(R.id.emailOfBilling)
                    emailName.setText(nameOfEmail)




//                    val orders = customerResponse?.data ?: emptyList()
//                    val latestCustomers = order.sortedByDescending { it.createdAt }
////                    println("Latest Orders of OrderHistory are: $latestCustomers")
//                    orderHistoryss.addAll(customerResponse?.data ?: emptyList())


//                    oorderHistorys = latestCustomers // Store fetched data in originalOrders
//                    adapter = RecycleAdapterOrder(originalOrders) // Initialize adapter with original data
//                    recyclerView.adapter = adapter

                } else {
                    Toast.makeText(
                        this@Billings,
                        "Error in Data Feching From OrderHistory....",
                        Toast.LENGTH_SHORT
                    ).show()
                    println("Error in Data Feching From OrderHistory..... ${response}")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Billings, "Network Error", Toast.LENGTH_SHORT)
                        .show()
                }

                println("Checking Error Responce in ExportPdf Button ${e.message}")
            }
            progressBar.visibility=View.GONE
        }

//        val orderHistoryResponce: OrderHistoryResponce = customerResponse?.data ?: emptyList()



        exportPdfButton.setOnClickListener {
            // Permission already granted, proceed with file saving


//            val filteredOrders = OrderValue.createdAt.filter { orderValue ->
//                val createdAtDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(orderValue.createdAt)
//                createdAtDate?.after(startDate) == true && createdAtDate?.before(endDate) == true
//            }



                val startDate = getStartDateFromDatePicker()
                val endDate = getEndDateFromDatePicker()
            print("OrderValue ${orderHistoryss} .... ..... ${oorderHistorys}")

            fun filterOrderValuesByDate(orderValues: List<OrderValue>, startDate: Date, endDate: Date): List<OrderValue> {
                val dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()) // Adjust theformat if needed

                return orderValues.filter { orderValue ->
                    try {
                        val createdAtDate = dateFormatter.parse(orderValue.createdAt)
                        val customDate = dateFormatter.parse(orderValue.customDate)
                        (createdAtDate?.let {
                            it.after(startDate) && it.before(endDate)
                        } ?: false) ||  (customDate?.let { it.after(startDate) && it.before(endDate) } ?: false)
                    } catch (e: ParseException) {
                        false // Handle parsing errors
                    }
                }
            }

                val filteredOrders = filterOrderValuesByDate(orderHistoryss, startDate, endDate)
//                    filterOrdersByDate(orderHistoryss, startDate, endDate)  // error in this line
                println("Filtered Orders checking : $filteredOrders")
                createPdf(this,  filterOrderValuesByDate(orderHistoryss, startDate, endDate))




        }

//        val billingName = nameOfEmail
//
//        val emailName = findViewById<TextView>(R.id.emailOfBilling)



        val backBTN = findViewById<ImageButton>(R.id.backBTN)
        backBTN.setOnClickListener {
            val homePage = Intent(this, AllOrders::class.java)
            startActivity(homePage)
        }
        val logoutBTN = findViewById<AppCompatButton>(R.id.logoutBtn)
        logoutBTN.setOnClickListener {

            val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.remove("AccessToken")
            editor.apply()

            val loginPage = Intent(this, MainActivity::class.java)
            startActivity(loginPage)

        }
    }


    private fun getStartDateFromDatePicker(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(startDatePicker.year, startDatePicker.month, startDatePicker.dayOfMonth)
        val startDate = calendar.time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        println("StartDate: ${startDate}")
        return startDate
    }

    private fun getEndDateFromDatePicker(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(endDatePicker.year, endDatePicker.month, endDatePicker.dayOfMonth)
        val endDate = calendar.time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        println("EndDate: ${endDate}")
        return endDate
    }


    private fun createPdf(context: Context, filteredOrders: List<OrderValue>) {
        try {

//            val nameOfPdf =
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            var totalPrice = 0.0
            val document = Document()
            val fileName = " $nameOfCustomer- $currentDate- $totalPrice Billings.pdf"

            val downloadsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//            val filePath =
//                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.absolutePath + "/" + fileName
            val filePath = File(downloadsDir, fileName).absolutePath
            PdfWriter.getInstance(document, FileOutputStream(filePath))

            document.open()


//            Renaming of Pdf File name


            val imageDrawable = ContextCompat.getDrawable(context, R.drawable.icepdf)
            val bitmap = imageDrawable?.toBitmap()
            val stream = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()
            // 4. Create iText Image object
            val image = Image.getInstance(byteArray, false) // false for recoverFromImageError, adjust as needed

            // 5. Add image to the document

                // Set image properties (position, size, etc.) if needed
                // ...
                // Set image properties (e.g., position and size)
            image.setAbsolutePosition(50f, 600f) // Adjust coordinates as needed
            image.scaleAbsolute(500f, 200f) // Adjust dimensions as needed

            document.add(image)



            // Set image properties (e.g., position and size)
//            pdfImage.setAbsolutePosition(50f, 750f) // Adjust coordinates as needed
//            pdfImage.scaleAbsolute(100f, 100f) // Adjust dimensions as needed



            document.add(Paragraph("  "))

//            val baseFont = BaseFont.createFont("res/font/english_with_indian_rupee.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED)
//            val fontfamely = Font(baseFont)



            //Create table
            val table = PdfPTable(5)// 8 columns : Name .Email, Category, Kg,price
            table.widthPercentage = 100f
            table.spacingBefore = 200f
//            table.defaultCell.horizontalAlignment = Element.ALIGN_CENTER
//            table.defaultCell.verticalAlignment = Element.ALIGN_MIDDLE

            // Add header row
            val font = Font(Font.FontFamily.HELVETICA, 13f, Font.BOLD)
            val headerCells = arrayOf(
                "S.NO",
                "DATE",
                "WEIGHT",
                "AMOUNT",
                "Packing Charge"
            )
            for (header in headerCells) {
                val cell = PdfPCell(Phrase(header, font))
                cell.horizontalAlignment = Element.ALIGN_CENTER
                table.addCell(cell)
            }
            var sNo = 1
            var packingCharge = 10


            // Add customer data rows
            println("Customer data rows Adding...... $filteredOrders")
            for (order in filteredOrders) {
                table.addCell(sNo.toString())

                // Date Formating in yyyy-mm-dd

                val customDate = order.customDate


                var originalDate = order.createdAt // Assuming order.createdAt contains the original date string

                if (customDate == null || customDate.isNullOrEmpty()){
                    originalDate = order.createdAt
                } else {
                    originalDate = customDate
                }
                val inputDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val outputDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

                val date = inputDateFormat.parse(originalDate) // Parse the original date
                val formattedDate = outputDateFormat.format(date) // Format the date



                table.addCell(formattedDate)// date
                table.addCell(order.quantity.toString()+"Kg")// kg in  Order class
                table.addCell(Phrase("Rs. "+ order.value.toString()))// Price in  Order class
                table.addCell("Rs. "+packingCharge.toString())
//
//
                totalPrice += order.value + packingCharge
                sNo++

                println("Added customer data row in PDF")
            }



//            document.add(Paragraph("Total Price:        $totalPrice"))

//            val totalRow = PdfPTable(5)
//
//            totalRow.widthPercentage = 100f
//
//            totalRow.addCell(PdfPCell(Phrase(""))) // Empty cell for S.NO
//            totalRow.addCell(PdfPCell(Phrase(""))) // Empty cell for DATE
//            totalRow.addCell(PdfPCell(Phrase(""))) // Empty cell for WEIGHT
//            totalRow.addCell(PdfPCell(Phrase("Total:")))
//            totalRow.addCell(PdfPCell(Phrase(totalPrice.toString())))
//
////            val totalPriceCell = PdfPCell(Phrase("Total Price:   $totalPrice"))
//
//            table.addCell(totalRow)


            for (i in 1..3) {
                table.addCell("")
            }
            table.addCell("Total Price:")
            table.addCell(Phrase("Rs. "+"$totalPrice"))


            println("Not Added customer Data....")


            // Add table to document
            document.add(table)

            val finalFileName = "$nameOfCustomer-$currentDate-($totalPrice).pdf"
            val finalFilePath = File(downloadsDir, finalFileName).absolutePath
            val tempFile = File(filePath)
            val finalFile = File(finalFilePath)
            tempFile.renameTo(finalFile)

            document.close()

            Toast.makeText(context, "PDF saved to $filePath", Toast.LENGTH_LONG).show()
            println("PDF saved to $filePath")


        } catch (e: Exception) {
            Toast.makeText(context, "Error creating PDF: ${e.message}", Toast.LENGTH_LONG).show()
            println("Error creating PDF: ${e.message}")
            // Handle the exception, e.g., show an error message
        }
    }


//    private fun filterOrdersByDate(
//        orderHistoryss: List<OrderValue>,
//        startDate: Date,
//        endDate: Date
//    ): List<OrderValue> {
//
//        println("Checking filter by date: $orderHistoryss  ....       ....$startDate.... ....$endDate")
//
//        val dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
//        return orderHistoryss.filter { histry ->
//            try {
//                val createdAtDate = dateFormatter.parse(histry.createdAt)
//                createdAtDate?.let {
//                    it.after(startDate) && it.before(endDate) // Include time in comparison
//                } ?: true
//            } catch (e: ParseException) {
//                println("Error in FilterByDate: ${e.message}")
//                false // Handle parsing errors
//            }
//
//
//        }
//    }

//    val filteredOrders = OrderValue.createdAt.filter { orderValue ->
//        val createdAtDate = SimpleDateFormat(
//            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
//            Locale.getDefault()
//        ).parse(orderValue.createdAt)
//        createdAtDate?.after(startDate) == true && createdAtDate?.before(endDate) == true
//    }


//    private fun filterOrders(query: String?) {
//        val filteredList = if (query.isNullOrEmpty()) {
//            originalOrders
//        } else {
//            originalOrders.filter { order ->
//                order.customer.fullName.contains(query, ignoreCase = true) ||
//                        order.customer.email.contains(query, ignoreCase = true)
//            }
//        }
//        adapter.updateOrders(filteredList) // Update adapter with filtered list
//
//
//    }
}