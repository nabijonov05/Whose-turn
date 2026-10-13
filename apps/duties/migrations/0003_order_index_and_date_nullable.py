# Generated manually for order_index and nullable date

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('duties', '0002_initial'),
    ]

    operations = [
        migrations.AlterModelOptions(
            name='dutyschedule',
            options={'ordering': ['order_index', 'id']},
        ),
        migrations.AlterUniqueTogether(
            name='dutyschedule',
            unique_together=set(),
        ),
        migrations.AddField(
            model_name='dutyschedule',
            name='order_index',
            field=models.PositiveIntegerField(default=0, verbose_name='Navbat ketma-ketlik indeksi'),
        ),
        migrations.AlterField(
            model_name='dutyschedule',
            name='date',
            field=models.DateField(blank=True, null=True, verbose_name='Navbat sanasi'),
        ),
    ]
