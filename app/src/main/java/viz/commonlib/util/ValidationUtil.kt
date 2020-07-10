package viz.commonlib.util

import com.viz.tools.l
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern


/**
 * @title: ValidationUtil
 * @projectName InsuranceDoubleRecord
 * @description:
 * @author wei
 * @date 2020-04-21 14:30
 */
object ValidationUtil {
    //数字表达式
    private val number_pattern: Pattern = Pattern.compile("^[0-9]*$")

    //手机号表达式
    private val phone_pattern =
        Pattern.compile("^(1)\\d{10}$")

    /**
     * 验证身份证号码是否正确
     * @param no 身份证号码
     * @return boolean
     */
    fun isIDCard(no: String): Boolean {
        val IDCardNo = no.toLowerCase(Locale.ROOT)
        //记录错误信息	
        var errmsg = ""
        val ValCodeArr =
            arrayOf("1", "0", "x", "9", "8", "7", "6", "5", "4", "3", "2")
        val Wi = arrayOf(
            "7",
            "9",
            "10",
            "5",
            "8",
            "4",
            "2",
            "1",
            "6",
            "3",
            "7",
            "9",
            "10",
            "5",
            "8",
            "4",
            "2"
        )
        var Ai = ""

        //================ 身份证号码的长度 15位或18位 ================
        if (IDCardNo.length != 15 && IDCardNo.length != 18) {
            errmsg = "身份证号码长度应该为15位或18位!"
            l.e("AppValidationMgr-->>isIDCard", errmsg)
            return false
        }

        //================ 数字 除最后以为都为数字 ================
        if (IDCardNo.length == 18) {
            Ai = IDCardNo.substring(0, 17)
        } else if (IDCardNo.length == 15) {
            Ai = IDCardNo.substring(0, 6) + "19" + IDCardNo.substring(6, 15)
        }
        if (!isNumber(Ai)) {
            errmsg = "身份证15位号码都应为数字 ; 18位号码除最后一位外，都应为数字"
            l.e("AppValidationMgr-->>isIDCard", errmsg)
            return false
        }

        //================ 出生年月是否有效 ================
        //年份
        val strYear = Ai.substring(6, 10)
        //月份
        val strMonth = Ai.substring(10, 12)
        //日
        val strDay = Ai.substring(12, 14)
        if (getDateIsTrue(strYear, strMonth, strDay) === false) {
            errmsg = "身份证生日无效"
            l.e("AppValidationMgr-->>isIDCard", errmsg)
            return false
        }
        val gc = GregorianCalendar()
        val s = SimpleDateFormat("yyyy-MM-dd")
        try {
            if (gc.get(Calendar.YEAR) - strYear.toInt() > 150 || gc.getTime()
                    .getTime() - s.parse("$strYear-$strMonth-$strDay").getTime() < 0
            ) {
                errmsg = "身份证生日不在有效范围"
                l.e("AppValidationMgr-->>isIDCard", errmsg)
                return false
            }
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            errmsg = "身份证生日不在有效范围"
            l.e("AppValidationMgr-->>isIDCard", errmsg + e.message)
            return false
        } catch (e1: ParseException) {
            e1.printStackTrace()
            errmsg = "身份证生日不在有效范围"
            l.e("AppValidationMgr-->>isIDCard", errmsg + e1.message)
            return false
        }
        if (strMonth.toInt() > 12 || strMonth.toInt() == 0) {
            errmsg = "身份证月份无效"
            l.e("AppValidationMgr-->>isIDCard", errmsg)
            return false
        }
        if (strDay.toInt() > 31 || strDay.toInt() == 0) {
            errmsg = "身份证日期无效"
            l.e("AppValidationMgr-->>isIDCard", errmsg)
            return false
        }

        //================ 地区码时候有效 ================
        val hashtable = getAreaCodeAll()
        if (hashtable[Ai.substring(0, 2)] == null) {
            errmsg = "身份证地区编码错误"
            l.e("AppValidationMgr-->>isIDCard", errmsg)
            return false
        }

        //================ 判断最后一位的值 ================
        var TotalmulAiWi = 0
        for (i in 0..16) {
            TotalmulAiWi = TotalmulAiWi + Ai[i].toString().toInt() * Wi[i].toInt()
        }
        val modValue = TotalmulAiWi % 11
        val strVerifyCode = ValCodeArr[modValue]
        Ai = Ai + strVerifyCode
        if (IDCardNo.length == 18) {
            if (Ai == IDCardNo == false) {
                errmsg = "身份证无效，不是合法的身份证号码"
                l.e("AppValidationMgr-->>isIDCard", errmsg)
                return false
            }
        } else {
            return true
        }
        return true
    }

    /**
     * 检查日期是否有效
     * @param year 年
     * @param month 月
     * @param day 日
     * @return boolean
     */
    fun getDateIsTrue(
        year: String,
        month: String,
        day: String
    ): Boolean {
        try {
            val data = year + month + day
            val simpledateformat =
                SimpleDateFormat("yyyyMMdd")
            simpledateformat.isLenient = false
            simpledateformat.parse(data)
        } catch (e: ParseException) {
            e.printStackTrace()
            l.e("AppSysDateMgr-->>getDateIsTrue", e.message.toString())
            return false
        }
        return true
    }

    /**
     * 验证是数字
     * @param str 验证字符
     * @return boolean
     */
    fun isNumber(str: String?): Boolean {
        return number_pattern.matcher(str).matches()
    }

    /**
     * 获取身份证号所有区域编码设置
     * @return Hashtable
     */
    fun getAreaCodeAll(): Hashtable<String, String> {
        val hashtable = Hashtable<String, String>()
        hashtable["11"] = "北京"
        hashtable["12"] = "天津"
        hashtable["13"] = "河北"
        hashtable["14"] = "山西"
        hashtable["15"] = "内蒙古"
        hashtable["21"] = "辽宁"
        hashtable["22"] = "吉林"
        hashtable["23"] = "黑龙江"
        hashtable["31"] = "上海"
        hashtable["32"] = "江苏"
        hashtable["33"] = "浙江"
        hashtable["34"] = "安徽"
        hashtable["35"] = "福建"
        hashtable["36"] = "江西"
        hashtable["37"] = "山东"
        hashtable["41"] = "河南"
        hashtable["42"] = "湖北"
        hashtable["43"] = "湖南"
        hashtable["44"] = "广东"
        hashtable["45"] = "广西"
        hashtable["46"] = "海南"
        hashtable["50"] = "重庆"
        hashtable["51"] = "四川"
        hashtable["52"] = "贵州"
        hashtable["53"] = "云南"
        hashtable["54"] = "西藏"
        hashtable["61"] = "陕西"
        hashtable["62"] = "甘肃"
        hashtable["63"] = "青海"
        hashtable["64"] = "宁夏"
        hashtable["65"] = "新疆"
        hashtable["71"] = "台湾"
        hashtable["81"] = "香港"
        hashtable["82"] = "澳门"
        hashtable["91"] = "国外"
        return hashtable
    }

    /**
     * 验证手机号是否正确
     * @param phone 手机号码
     * @return boolean
     */
    fun isPhone(phone: String?): Boolean {
        return phone_pattern.matcher(phone).matches()
    }
}