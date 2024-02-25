package com.cureius.pocket.feature_transaction.presentation.transactions

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sort
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.cureius.pocket.R
import com.cureius.pocket.feature_transaction.presentation.add_transaction.AddTransactionViewModel
import com.cureius.pocket.feature_transaction.presentation.transactions.components.OrderSection
import com.cureius.pocket.feature_transaction.presentation.transactions.components.TransactionItem
import com.cureius.pocket.feature_transaction.presentation.util.components.CameraPermissionTextProvider
import com.cureius.pocket.feature_transaction.presentation.util.components.PermissionDialog
import com.cureius.pocket.feature_transaction.presentation.util.components.PhoneCallPermissionTextProvider
import com.cureius.pocket.feature_transaction.presentation.util.components.RecordAudioPermissionTextProvider
import com.cureius.pocket.util.components.MonthPicker
import kotlinx.coroutines.launch
import java.util.Calendar

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun TransactionsScreen(
    navController: NavController?,
    viewModel: TransactionsViewModel = hiltViewModel(),
    addViewModel: AddTransactionViewModel = hiltViewModel()
) {
    val state = viewModel?.state?.value
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val dialogQueue = viewModel?.visiblePermissionDialogQueue

    val permissionsToRequest = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CALL_PHONE,
    )

    val cameraPermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            viewModel.onPermissionResult(
                permission = Manifest.permission.READ_SMS,
                isGranted = isGranted
            )
        }
    )

    val multiplePermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { perms ->
            permissionsToRequest.forEach { permission ->
                viewModel.onPermissionResult(
                    permission = permission,
                    isGranted = perms[permission] == true
                )
            }
        }
    )

    Scaffold(
        floatingActionButton = {},
        scaffoldState = scaffoldState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transactions", style = MaterialTheme.typography.h4
                )
                Row {
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colors.primary.copy(
                                    alpha = 0.1f
                                ), RoundedCornerShape(12.dp)
                            )
                            .padding(8.dp)
                            .clickable {
                                viewModel.onEvent(TransactionsEvent.ToggleMonthPickerDialog)
                            }, contentAlignment = Alignment.Center
                    ) {
                        val config =
                            ImageVector.vectorResource(id = R.drawable.outline_filter_alt_24)
                        Icon(
                            imageVector = config,
                            contentDescription = "filter",
                            tint = MaterialTheme.colors.onBackground,
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = {
                        viewModel?.onEvent(TransactionsEvent.ToggleOrderSection)
                    }) {
                        Icon(imageVector = Icons.Default.Sort, contentDescription = "Sort")
                    }
                }
            }
            if (state != null) {
                AnimatedVisibility(
                    visible = state.isOrderSelectionVisible,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    OrderSection(modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                        transactionOrder = state.transactionOrder,
                        onOrderChange = {
                            viewModel.onEvent(TransactionsEvent.Order(it))
                        })
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (state != null) {
                    itemsIndexed(if (viewModel.monthPicked.value != null) state.transactionsOnCurrentMonthForAccounts else state.transactionsForAccounts) { index, transaction ->
                        TransactionItem(transaction = transaction, modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
//                                navController?.navigate(Screen.AddTransactionScreen.route + "?transactionId=${transaction.id}&transactionColor=${transaction.color}")
                            }, onDeleteClick = {
                            viewModel.onEvent(TransactionsEvent.DeleteTransaction(transaction))
                            scope.launch {
                                val result = scaffoldState.snackbarHostState.showSnackbar(
                                    message = "Transaction Deleted..!", actionLabel = "Undo"
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.onEvent(TransactionsEvent.RestoreTransaction)
                                }
                            }
                        },
                            showDate = if (index == 0) true else {
                                if (viewModel.monthPicked.value != null) state.transactionsOnCurrentMonthForAccounts[index - 1].date != transaction.date else state.transactionsForAccounts[index - 1].date != transaction.date
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    item {
                        Spacer(modifier = Modifier.height(160.dp))
                    }
                }

            }
        }

        dialogQueue.reversed().forEach { permission ->
            PermissionDialog(
                permissionTextProvider = when (permission) {
                    Manifest.permission.CAMERA -> {
                        CameraPermissionTextProvider()
                    }

                    Manifest.permission.RECORD_AUDIO -> {
                        RecordAudioPermissionTextProvider()
                    }

                    Manifest.permission.CALL_PHONE -> {
                        PhoneCallPermissionTextProvider()
                    }

                    else -> return@forEach
                },
                isPermanentlyDeclined = !shouldShowRequestPermissionRationale(
                    LocalContext.current as Activity,
                    permission
                ),
                onDismiss = viewModel::dismissDialog,
                onOkClick = {
                    viewModel.dismissDialog()
                    multiplePermissionResultLauncher.launch(
                        arrayOf(permission)
                    )
                },
                onGoToAppSettingsClick = { }
            )
        }

        if (viewModel.monthPickerDialogVisibility.value) {
            var visible by remember {
                mutableStateOf(true)
            }

            var date by remember {
                mutableStateOf("")
            }

            val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
            val year = Calendar.getInstance().get(Calendar.YEAR)

            // A surface container using the 'background' color from the theme
            MonthPicker(visible = visible,
                currentMonth = currentMonth,
                currentYear = year,
                confirmButtonCLicked = { month_, year_ ->
                    date = "$month_/$year_"
                    viewModel.onEvent(TransactionsEvent.MonthSelected(date))
                },
                cancelClicked = {
                    viewModel.onEvent(TransactionsEvent.ToggleMonthPickerDialog)
                })
        }
    }

}


@Preview(showSystemUi = true)
@Composable
fun TransactionsScreenPreview() {
    TransactionsScreen(
        navController = rememberNavController(),
    )
}