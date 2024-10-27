import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// Определяем сущность таблицы для друзей
@Entity(tableName = "friends")
data class FriendDB(
    @PrimaryKey val token: String,
    val name: String,
    val img: String,
    val online: Boolean,
    val lastMessage: String
)

// Интерфейс DAO для работы с данными друзей
@Dao
interface FriendDao {
    @Query("SELECT * FROM friends")
    fun getAllFriends(): Flow<List<FriendDB>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriends(friends: List<FriendDB>)

    @Query("DELETE FROM friends")
    suspend fun deleteAllFriends()
}

// База данных с Room
@Database(entities = [FriendDB::class], version = 1, exportSchema = false)
abstract class FriendDatabase : RoomDatabase() {
    abstract fun friendDao(): FriendDao

    companion object {
        @Volatile
        private var INSTANCE: FriendDatabase? = null

        // Функция для получения экземпляра базы данных
        fun getDatabase(context: Context): FriendDatabase {
            return INSTANCE ?: synchronized(this) {
                // Если экземпляр базы данных еще не создан, создаем его
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FriendDatabase::class.java,
                    "friend_database"
                )
                    .fallbackToDestructiveMigration() // Опционально, добавляем миграцию
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
