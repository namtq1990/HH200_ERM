package Debug;

import java.util.ArrayList;

public class Test implements Source {

	public String Source_1 ="0,0,0,0,0,92,991,2230,2877,2981,2862,2910,2838,2978,2994,3101,3206,3295,3374,3697,3978,4067,4387,4583,4737,4988,5373,5458,5805,5925,6235,6324,6405,6856,6988,7139,7207,7497,7949,7960,8173,8223,8351,8373,8575,8793,8767,8727,8954,8732,8980,8732,8907,8946,8840,9015,8890,8959,8869,8945,8829,8828,8827,8540,8688,8578,8401,8291,8459,8213,8228,8244,8098,8098,8119,8280,8408,8285,8254,8299,8145,7841,7732,7621,7238,7086,6786,6750,6676,6575,6500,6380,6442,6443,6426,6489,6333,6370,6327,6411,6167,6035,5875,5733,5523,5457,5251,5169,5179,5225,5054,5191,5328,5381,5458,5569,5678,5631,5464,5459,5481,5123,5004,4713,4405,4203,3796,3574,3507,3323,3108,3056,2931,2994,2857,2781,2771,2755,2750,2531,2619,2565,2497,2529,2531,2489,2419,2425,2319,2418,2380,2327,2265,2291,2311,2239,2227,2204,2211,2270,2211,2230,2182,2140,2154,2152,2187,2127,2229,2172,2030,2135,2149,2122,2007,1992,2056,2079,2011,1993,1984,2015,2052,1863,1874,1878,1860,1848,1901,1975,1971,1974,1980,2040,2177,2229,2300,2326,2445,2454,2703,2786,2687,2857,2873,2907,2976,2872,2814,2866,2762,2601,2576,2442,2227,2046,1876,1896,1722,1535,1432,1371,1241,1175,1082,1125,1105,1068,1004,1018,998,935,975,982,914,999,988,1033,1042,993,952,971,977,921,943,934,973,971,896,958,941,969,959,895,962,991,927,952,905,982,1019,978,1019,998,945,962,947,956,947,938,935,933,878,848,920,811,837,808,841,790,783,762,773,766,770,776,766,701,759,694,710,724,746,701,709,694,719,702,759,682,707,727,728,724,751,715,766,743,800,694,756,831,766,793,761,735,803,713,753,798,727,789,720,709,675,752,695,679,669,658,718,676,627,652,584,647,632,573,575,563,520,529,496,541,555,506,483,458,487,447,415,448,449,459,477,433,428,470,453,469,484,462,481,492,475,491,524,479,540,500,480,512,532,587,599,594,616,621,599,608,658,683,623,665,684,675,640,658,626,613,571,571,564,586,534,525,520,473,509,474,476,465,435,405,384,400,403,343,379,388,380,376,369,372,363,376,401,370,393,386,376,412,391,377,380,391,413,388,410,393,366,427,371,391,389,374,342,335,366,376,359,330,352,328,283,327,317,304,293,311,278,304,310,264,301,287,294,300,310,290,322,300,333,282,317,287,316,349,304,324,344,338,346,333,335,351,357,358,327,370,360,382,339,347,348,343,323,339,349,309,331,292,321,300,321,325,298,300,288,328,281,290,247,267,259,234,282,288,251,245,245,246,244,235,236,245,245,236,259,209,229,240,205,207,201,247,212,224,212,203,191,197,186,207,198,221,200,197,195,174,169,245,195,167,184,177,184,209,165,212,180,196,170,195,156,173,166,190,189,166,173,154,161,175,171,176,176,189,156,151,195,171,174,170,162,211,182,195,198,214,196,207,206,208,207,218,232,239,246,244,214,271,219,267,260,276,257,308,263,300,290,286,288,343,298,291,298,324,310,302,283,274,294,273,294,296,286,274,250,237,218,235,263,228,210,203,219,191,160,203,173,166,160,150,133,138,138,128,122,116,110,114,119,104,109,120,107,107,91,81,77,94,72,81,89,90,102,78,89,97,68,85,68,77,81,63,85,62,74,67,68,72,68,62,70,58,79,69,58,61,69,58,64,75,48,59,68,48,71,54,61,66,68,58,68,53,85,60,46,69,71,58,68,71,76,63,66,76,79,84,72,64,63,76,73,79,77,85,70,80,71,90,97,83,86,88,100,81,80,72,71,85,88,81,88,92,97,95,77,91,88,109,91,96,102,110,104,97,84,117,92,109,98,113,94,87,110,103,85,107,85,87,103,91,97,71,97,111,79,88,72,74,90,89,87,71,73,73,93,71,67,59,55,63,77,50,54,71,71,57,49,53,41,64,53,39,44,40,36,33,52,39,45,35,47,39,44,37,42,34,42,41,27,29,39,39,23,39,39,30,37,33,25,47,35,35,31,35,33,40,40,25,37,33,38,36,23,20,22,34,29,36,43,40,29,30,30,29,28,33,39,32,43,53,45,52,23,34,54,44,38,28,56,37,31,46,34,37,52,35,50,38,42,42,46,52,54,60,61,61,65,62,59,51,72,74,69,74,82,74,82,89,76,52,73,89,63,95,80,77,74,79,74,62,95,67,84,55,84,62,80,70,68,62,67,55,59,53,56,43,35,45,52,34,31,35,32,21,26,24,26,17,18,25,16,14,2,9,6,10,4,7,7,2,3,5,3,2,2,6,4,3,3,1,3,2,3,2,2,3,0,2,2,0,4,0,0,1,1,3,1,1,0,0,0,2,1,1,0,0,3,1,0,0,2,1,1,0,2,1,0,0,0,1,0,2,0,1,1,0,4,2,0,1,0,0,0,1,2,0,1,0,1,0,1,2,1,0,1,1,3,2,0,0";
			//"0,0,0,0,0,0,4,9,12,12,12,12,12,12,12,13,13,14,14,15,17,17,18,19,20,21,22,23,24,25,26,26,27,29,29,30,30,31,33,33,34,34,35,35,36,37,37,36,37,36,37,36,37,37,37,38,37,37,37,37,37,37,37,36,36,36,35,35,35,34,34,34,34,34,34,35,35,35,34,35,34,33,32,32,30,30,28,28,28,27,27,27,27,27,27,27,26,27,26,27,26,25,24,24,23,23,22,22,22,22,21,22,22,22,23,23,24,23,23,23,23,21,21,20,18,18,16,15,15,14,13,13,12,12,12,12,12,11,11,11,11,11,10,11,11,10,10,10,10,10,10,10,9,10,10,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,8,9,9,9,8,8,9,9,8,8,8,8,9,8,8,8,8,8,8,8,8,8,8,9,9,9,10,10,10,10,11,12,11,12,12,12,12,12,12,12,12,11,11,10,9,9,8,8,7,6,6,6,5,5,5,5,5,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,3,3,3,4,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,2,3,3,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,3,3,2,3,3,3,3,3,3,3,3,3,3,3,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1,1,2,2,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,2,2,2,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0";

	
	
	public String Background =
			"0,0,0,0,2,34,173,354,453,475,498,536,597,667,758,867,989,1138,1272,1377,1501,1652,1743,1768,1803,1848,1890,1902,1872,1845,1827,1812,1817,1813,1787,1758,1715,1659,1597,1569,1553,1527,1471,1401,1346,1332,1285,1251,1233,1198,1162,1132,1102,1059,1027,1016,1008,993,961,929,917,895,864,839,817,810,818,818,802,787,769,746,732,737,752,756,737,696,648,611,586,578,560,543,531,514,492,474,461,454,445,433,432,435,427,416,414,415,404,391,393,393,383,365,346,338,326,316,322,333,326,320,311,304,309,312,316,304,283,270,266,257,237,225,229,240,242,229,210,204,207,207,210,216,213,198,193,200,206,204,194,184,178,177,173,170,174,178,180,178,169,165,165,165,167,160,149,154,160,157,152,149,145,141,146,154,157,155,152,149,147,143,141,142,142,141,140,137,134,133,129,125,126,126,126,129,133,130,134,147,153,152,148,144,144,142,134,131,132,130,126,125,120,117,116,112,106,105,104,100,95,92,90,86,83,83,84,82,79,81,84,83,83,83,80,77,77,77,79,80,80,81,81,84,78,73,81,84,79,72,70,81,79,72,76,79,83,83,75,67,70,72,71,72,72,69,67,66,66,69,70,68,66,66,66,65,65,65,66,66,65,64,64,62,58,57,58,57,56,57,58,64,69,67,64,67,66,61,60,66,73,69,60,57,61,62,63,66,68,68,68,66,64,61,59,60,62,62,60,61,62,64,67,69,69,63,58,58,60,61,58,55,52,52,55,57,53,51,54,55,53,54,54,49,44,43,46,45,44,41,41,46,48,49,48,49,46,43,42,38,36,37,41,44,48,51,49,44,42,47,48,51,49,44,42,43,46,51,50,51,47,46,47,50,50,47,43,41,43,45,43,41,40,38,35,36,37,37,40,42,41,42,39,31,25,27,30,29,29,32,35,32,29,30,31,31,34,35,32,31,28,26,24,27,29,26,22,21,24,25,23,24,25,26,24,20,19,20,20,19,19,18,24,25,25,24,24,21,18,17,20,21,22,22,22,22,24,26,27,29,31,34,36,37,35,31,31,35,40,41,42,47,55,58,52,53,63,68,71,79,86,82,80,79,98,94,91,96,83,87,109,101,96,94,94,84,74,72,65,68,77,74,66,62,52,44,45,45,40,30,23,20,20,20,18,17,19,19,14,12,12,12,12,11,10,10,12,12,10,10,8,6,9,13,12,9,7,7,9,8,6,6,7,6,7,7,7,7,6,5,5,7,8,7,7,7,8,8,7,5,4,6,7,8,8,8,7,6,5,6,8,8,6,4,5,6,6,6,6,8,8,7,6,7,8,8,6,5,6,8,9,10,12,13,11,7,7,7,7,8,7,5,5,4,4,4,5,7,5,5,6,5,4,3,5,7,7,4,3,3,4,4,4,5,5,5,6,6,5,5,4,3,2,3,6,6,5,6,6,5,3,3,4,6,7,7,4,2,1,1,1,1,1,1,1,1,2,3,4,4,3,2,2,2,3,4,4,4,4,4,4,5,5,5,4,3,3,4,4,3,3,3,5,4,4,4,5,4,2,2,3,3,3,3,3,4,4,4,4,4,4,4,4,3,4,5,6,6,6,5,4,4,4,4,4,4,3,4,5,5,5,4,4,4,3,4,5,5,4,3,2,2,3,4,4,4,3,2,3,5,5,5,6,6,6,6,4,3,4,6,6,4,3,2,3,3,3,4,5,4,3,3,4,5,4,2,2,2,3,2,3,4,4,2,2,3,3,2,2,2,3,3,4,4,4,3,2,2,2,2,1,1,2,2,3,4,3,3,2,2,2,3,3,2,1,1,2,3,2,1,1,2,2,2,2,2,1,1,1,1,2,2,1,1,1,1,1,1,2,2,2,1,1,2,1,1,1,1,2,3,3,2,1,2,2,1,2,4,4,5,4,4,5,6,7,6,5,5,5,5,5,6,8,8,9,9,8,7,8,7,7,7,5,5,7,8,8,8,7,6,5,6,7,8,6,4,5,7,6,4,4,3,3,3,2,2,3,3,3,3,3,3,3,3,2,1,2,2,1,1,1,2,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0";
	@Override
	public String[] GetSource() {

		String[] K40 = new String[] { Source_1 };

		return K40;
	}

	@Override
	public String GetBackground() {
		// TODO Auto-generated method stub
		return Background;
	}

	@Override
	public double[] Coefficients() {
		double[] Coeffecince = new double[] { -2.386465115E-6, 2.888416051, 11.20013517 };

		return Coeffecince;
	}
	@Override
	public int[] GetNeutron() {
		int[] Neutron = new int[] {1,2,1,2,1,2,3,4,1,2,3,4,5,6,7,8,4,3,2,1,2,3,4,5,4,3,2,1,2,3};
		return Neutron;
	}
}
