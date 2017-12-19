1.03.16
1:update iso_additional set service_id = '0' where service_id = 'A54212' and iso_element = 'nama' and influx = 1
1:update iso_additional set iso_seq = 2 where service_id = 'A54212' and iso_element = 'sal_amount' and influx = 1
2:update iso_data set iso_bit_uid = 57, iso_value = '@POSBRI#' where service_id = 'A54A10' and influx = 1 and iso_bit_uid = 37
2:update iso_data set iso_value = '000000' where service_id = 'A54A10' and influx = 1 and iso_bit_uid = 62
2:update iso_data set iso_bit_uid = 22, iso_value = '0901', meta_length = 2 where service_id = 'A54A10' and influx = 1 and iso_bit_uid = 12
2:update iso_data set iso_bit_uid = 25, iso_value = '00', meta_length = 2 where service_id = 'A54A10' and influx = 1 and iso_bit_uid = 13
2:update iso_data set iso_bit_uid = 62, iso_value = '000000', meta_length = 2 where service_id = 'A54A20' and influx = 1 and iso_bit_uid = 37
3:update screen_component set screen_id = '533000X' where screen_id = '541200F' and comp_id = '54119'
3:update screen_component set sequence = 4 where screen_id = '541200F' and comp_id = '54120'
3:update screen_component set sequence = 5 where screen_id = '541200F' and comp_id = '54121'
3:update screen_component set sequence = 6 where screen_id = '541200F' and comp_id = '54114'
3:update screen_component set sequence = 7 where screen_id = '541200F' and comp_id = '54115'
3:update screen_component set sequence = 7 where screen_id = '543120F' and comp_id = '54326'
3:update screen_component set sequence = 8 where screen_id = '543120F' and comp_id = '54327'
3:update screen_component set sequence = 9 where screen_id = '543120F' and comp_id = 'I0010'
3:update screen_component set sequence = 10 where screen_id = '543120F' and comp_id = '54332'
3:update screen_component set sequence = 11 where screen_id = '543120F' and comp_id = '54324'
3:update screen_component set sequence = 12 where screen_id = '543120F' and comp_id = '54325'
3:update screen_component set sequence = 13 where screen_id = '543120F' and comp_id = 'I0011'
3:update screen_component set screen_id = '533000X' where screen_id = '543120F' and comp_id = '5431E'
3:update screen_component set sequence = 14 where screen_id = '543120F' and comp_id = '5431D'
3:update screen_component set sequence = 16 where screen_id = '543120F' and comp_id = '54328'
3:update screen_component set sequence = 17 where screen_id = '543120F' and comp_id = '54329'
3:update screen_component set sequence = 18 where screen_id = '543120F' and comp_id = '5432A'
3:update screen_component set sequence = 19 where screen_id = '543120F' and comp_id = '5431C'
3:update screen_component set sequence = 20 where screen_id = '543120F' and comp_id = '54366'
3:update screen_component set sequence = 21 where screen_id = '543120F' and comp_id = '54331'
3:update component set comp_act = 'nom_tagihan' where comp_id = '54322'
3:update component set comp_act = 'nom_tagihan+nom_admin' where comp_id = '54327'
3:update screen_component set sequence = 7 where screen_id = '543120E' and comp_id = '54326'
3:update screen_component set sequence = 8 where screen_id = '543120E' and comp_id = '54327'
3:update screen_component set sequence = 9 where screen_id = '543120E' and comp_id = 'I0010'
3:update screen_component set sequence = 10 where screen_id = '543120E' and comp_id = '54332'
3:update screen_component set sequence = 11 where screen_id = '543120E' and comp_id = '54324'
3:update screen_component set sequence = 12 where screen_id = '543120E' and comp_id = '54325'
3:update screen_component set sequence = 13 where screen_id = '543120E' and comp_id = 'I0011'
3:delete from screen_component where screen_id = '543120E' and comp_id = '5431E'
3:update screen_component set sequence = 14 where screen_id = '543120E' and comp_id = '5431D'
3:update screen_component set sequence = 16 where screen_id = '543120E' and comp_id = '5431F'
3:update screen_component set sequence = 17 where screen_id = '543120E' and comp_id = '54329'
3:update screen_component set sequence = 18 where screen_id = '543120E' and comp_id = '5432A'
3:update screen_component set sequence = 19 where screen_id = '543120E' and comp_id = '5431C'
3:update screen_component set sequence = 20 where screen_id = '543120E' and comp_id = '54366'
3:update screen_component set sequence = 21 where screen_id = '543120E' and comp_id = '54331'
4:update service_meta set meta_id = 'rnom_admin' where service_id = 'A54312' and influx = 2 and seq = 6
4:update service_meta set meta_id = 'rnom_tagihan' where service_id = 'A54312' and influx = 2 and seq = 8
5:delete from screen_component where screen_id = '541100F' and comp_id = '54119'
5:update screen_component set sequence = 4 where screen_id = '541100F' and comp_id = 'I1004'
5:update screen_component set sequence = 5 where screen_id = '541100F' and comp_id = 'I0004'
6:insert into screen_component (screen_id, comp_id, sequence) select '340000F', comp_id, sequence from screen_component where screen_id = '350000F'
7:update iso_data set iso_bit_uid = 25, iso_value = '00', meta_length = 2 where service_id like 'A545%1' and iso_bit_uid = 12 and influx = 1
7:update iso_data set iso_bit_uid = 52, meta_length = 1 where service_id like 'A545%1' and iso_bit_uid = 13 and influx = 1
8:update iso_data set iso_bit_uid = 25, iso_value = '00', meta_length = 2 where service_id like 'A545%1' and iso_bit_uid = 12 and influx = 1
9:update iso_data set iso_bit_uid = 52, iso_value = null, meta_length = 1 where service_id like 'A545%0' and iso_bit_uid = 12 and influx = 1
10:insert into iso_data (channel_uid, iso_bit_uid, meta_type_uid, meta_length, influx, service_id, is_fixed, is_mandatory) values ('40000002', 35, 0, 1, 1, 'A54511', 'f', 'f')
10:insert into iso_data (channel_uid, iso_bit_uid, meta_type_uid, meta_length, influx, service_id, is_fixed, is_mandatory) values ('40000002', 35, 0, 1, 1, 'A54521', 'f', 'f')
10:insert into iso_data (channel_uid, iso_bit_uid, meta_type_uid, meta_length, influx, service_id, is_fixed, is_mandatory) values ('40000002', 35, 0, 1, 1, 'A54531', 'f', 'f')
10:insert into iso_data (channel_uid, iso_bit_uid, meta_type_uid, meta_length, influx, service_id, is_fixed, is_mandatory) values ('40000002', 35, 0, 1, 1, 'A54541', 'f', 'f')
10:insert into iso_data (channel_uid, iso_bit_uid, meta_type_uid, meta_length, influx, service_id, is_fixed, is_mandatory) values ('40000002', 35, 0, 1, 1, 'A54551', 'f', 'f')
10:insert into iso_data (channel_uid, iso_bit_uid, meta_type_uid, meta_length, influx, service_id, is_fixed, is_mandatory) values ('40000002', 35, 0, 1, 1, 'A54561', 'f', 'f')
11:update screen set screen_title = 'Shift 3' where screen_id like '3730%'
12:update component set visible = 't' where comp_id = '55620'
13:update screen set screen_title = 'CETAK TOKEN PRABAYAR' where screen_id = '543310F'
14:delete from screen_component where screen_id = '543220F' and comp_id = '54349'
14:update screen_component set sequence=10 where screen_id = '543220F' and comp_id = '54374'
14:update screen_component set sequence=11 where screen_id = '543220F' and comp_id = '54350'
14:update screen_component set sequence=12 where screen_id = '543220F' and comp_id = '54351'
14:update screen_component set sequence=13 where screen_id = '543220F' and comp_id = '54372'
14:update screen_component set sequence=14 where screen_id = '543220F' and comp_id = 'I0010'
14:update screen_component set sequence=15 where screen_id = '543220F' and comp_id = '54373'
14:update screen_component set sequence=16 where screen_id = '543220F' and comp_id = '54332'
14:update screen_component set sequence=17 where screen_id = '543220F' and comp_id = '54353'
14:update screen_component set sequence=18 where screen_id = '543220F' and comp_id = '54354'
15:update iso_additional set iso_seq = 25 where service_id = 'A54322' and influx = 2 and iso_element = 'nom_ppj'
15:update iso_additional set iso_seq = 18 where service_id = 'A54322' and influx = 2 and iso_element = 'rkode11'
15:update iso_additional set iso_seq = 19 where service_id = 'A54322' and influx = 2 and iso_element = 'nom_ppj'
15:update component set comp_act = 'nom_ppj' where comp_id = '54348'
16:update service_meta set meta_id = 'nom_ppj' where meta_id = 'ppj' and service_id = 'A54322' and influx = 2
17:update screen_component set sequence=19 where screen_id = '543220F' and comp_id = '54354'
17:update screen_component set sequence=18 where screen_id = '543220F' and comp_id = '54353'
17:update screen_component set sequence=17 where screen_id = '543220F' and comp_id = '54332'
17:update screen_component set sequence=16 where screen_id = '543220F' and comp_id = '54373'
17:update screen_component set sequence=15 where screen_id = '543220F' and comp_id = 'I0010'
17:update screen_component set sequence=14 where screen_id = '543220F' and comp_id = '54372'
17:update screen_component set sequence=13 where screen_id = '543220F' and comp_id = '54351'
17:update screen_component set sequence=12 where screen_id = '543220F' and comp_id = '54350'
17:update screen_component set sequence=11 where screen_id = '543220F' and comp_id = '54374'
17:insert into screen_component (screen_id, comp_id, sequence) values ('543220F', '54349', 10)
