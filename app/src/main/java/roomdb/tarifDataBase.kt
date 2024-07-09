package roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.burak.yemektarifi2.view.tarifFragmentArgs
import model.Tarif

@Database(entities = [Tarif::class], version = 1)
abstract class tarifDataBase : RoomDatabase() {
    abstract fun TarifDAO(): TarifDAO
}